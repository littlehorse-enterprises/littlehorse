# Proposal: Oneof Support in `Struct`s

- [Proposal: Oneof Support in `Struct`s](#proposal-oneof-support-in-structs)
  - [Background](#background)
  - [Proposed Solution](#proposed-solution)
    - [Client-Side (SDK) Changes](#client-side-sdk-changes)
      - [Declaring Oneofs in the SDKs](#declaring-oneofs-in-the-sdks)
      - [Required Selections and Unset Fields](#required-selections-and-unset-fields)
    - [Protobuf Changes](#protobuf-changes)
    - [Comparison with JSON Schema and Protobuf](#comparison-with-json-schema-and-protobuf)
  - [Backwards Compatibility](#backwards-compatibility)
  - [Alternatives](#alternatives)

Author: Jacob Snarr

This proposal will cover the following:

- Allow users to declare oneof constraints on fields in an existing Struct, or define a reusable oneof helper type.
- Support both APIs through the same protobuf representation and validation rules.

## Background

Take a payment request for example. A user can pay with a card token, a bank account ID, or a wallet ID, but they should only provide one. If we put these three fields on an ordinary `StructDef`, we cannot enforce that rule. Required fields would demand all three, while nullable fields would allow any combination.

We need a way to declare these fields as alternatives, so LittleHorse can reject invalid combinations and, when required, ensure one is provided.

## Proposed Solution

Users can declare a oneof group on individual fields or define a reusable oneof type. Equivalent models generate the same protobuf schema and values through either API.

### Client-Side (SDK) Changes

#### Declaring Oneofs in the SDKs

In **Java**, users will be able to declare a group on fields, getters, setters, or record components using `@LHStructOneOf`:

```java
@LHStructDef("payment")
public record Payment(
        String orderId,
        @LHStructOneOf(group = "method", required = true) String cardToken,
        @LHStructOneOf(group = "method", required = true) String bankAccountId,
        @LHStructOneOf(group = "method", required = true) String walletId) {}
```

Fields sharing the group name are mutually exclusive. `orderId` remains an ordinary field. Multiple groups can coexist in the same Struct, but a field can belong to only one group.

The same annotation on a class or record makes all its included fields alternatives. Omit `group` on types; it is required on properties:

```java
@LHStructOneOf(required = true)
public record PaymentMethod(
        String cardToken, String bankAccountId, String walletId) {}

@LHStructDef("payment")
public record Payment(String orderId, PaymentMethod method) {}

// Usage inside a method:
Payment payment = new Payment(
        "order-123", new PaymentMethod("tok_abc", null, null));
```

Both examples produce the same Struct field layout:

```json
{
  "orderId": "order-123",
  "method": {"cardToken": "tok_abc"}
}
```

- **Field groups:** `group = "method"` names the nested wire field. The SDK groups properties when writing and expands them when reading.
- **Registration:** Helper schemas are embedded in the parent. Combining type-level `@LHStructOneOf` with `@LHStructDef` is invalid: named StructDefs contain choices so they can later gain ordinary fields alongside them.
- **Java types:** Helpers can be public nested records, public static classes, or top-level types. POJOs use the same annotations and require public no-arg constructors, getters, and setters. Placement does not change the wire format.

| Difference | Group declared on individual fields | Separate oneof type |
|---------------|-------------------------------------|---------------------|
| Setup | Add metadata to existing fields; no new class. | Define another class/type and reference it. |
| Field access | `payment.cardToken()` in Java. | `payment.method().cardToken()` in Java. |
| Reuse | Repeat group declarations in each model. | Reuse `PaymentMethod` across models. |
| Mistakes | String typos can create unintended groups; settings are repeated. | Membership comes from the type's fields; the selection setting is declared once. |
| Wire paths | SDK creates `method.cardToken` from a flat Java property. | `method.cardToken` follows the model directly. |

The other SDKs will support both forms using their existing metadata conventions. Each will group and ungroup flat properties during conversion. Helper schemas are embedded and cannot have their own StructDef names in any SDK. For groups in an existing Struct:

| SDK | Declaration on each participating field |
|-----|----------------------------------------|
| Python | `Annotated[str \| None, LHStructOneOf(group="method", required=True)]`, alongside existing `LHStructField` metadata. |
| .NET | `[LHStructOneOf("method", required: true)]` on a property or accessor. |
| Go | `lhoneof:"method,required"` on an exported pointer field, alongside its `lh` / `json` tags. |
| JavaScript / TypeScript | `lhOneOfField(z.string(), { group: 'method', required: true })` on a field in `lhStruct`'s Zod object. |

JavaScript's `lhOneOfField` helper will make that field optional and preserve its metadata. The containing `lhStruct` will validate the groups during parsing and conversion. The object-level `lhOneOf` below will remain a separate helper, so the two forms are unambiguous.

For a separate type, **Python** will use `@lh_oneof`, following the existing [`@lh_struct_def`](../sdk-python/littlehorse/lh_struct.py) conventions:

```python
from littlehorse.lh_struct import lh_struct_def, lh_oneof

@lh_oneof(selection_required=True)
class PaymentMethod:
    card_token: str | None
    bank_account_id: str | None
    wallet_id: str | None

@lh_struct_def(name="payment")
class Payment:
    order_id: str
    method: PaymentMethod

payment = Payment(
    order_id="order-123", method=PaymentMethod(card_token="tok_abc")
)
```

`lh_oneof` will build an inline schema and generate a constructor that initializes omitted alternatives to `None`. Field-level markers will also allow omitted alternatives.

The SDK will unwrap `T | None` or `Optional[T]` for type mapping. `None` leaves the field unset; it does not make it nullable in protobuf. Explicit defaults and unions of multiple non-None types will be rejected.

In **.NET**, a separate inline type will use `[LHStructOneOf]`, following the existing [attribute conventions](../sdk-dotnet/LittleHorse.Sdk/Worker/LHStructDefAttribute.cs):

```csharp
[LHStructOneOf(required: true)]
public class PaymentMethod
{
    public string? CardToken { get; set; }
    public string? BankAccountId { get; set; }
    public string? WalletId { get; set; }
}

[LHStructDef("payment")]
public class Payment
{
    public string OrderId { get; set; } = null!;
    public PaymentMethod Method { get; set; } = null!;
}
```

Properties will use reference types or nullable value types: `int?` maps to `INT`, and null is omitted. We will keep the existing property-based serialization, without adding immutable-record constructors.

In **Go**, we will add `OneOf *LHStructOneOfInfo` to [`LHStructDefInfo`](../sdk-go/littlehorse/lh_struct_def.go). `LHStructOneOfInfo` will contain a `SelectionRequired bool` field:

```go
type PaymentMethod struct {
    CardToken     *string `json:"cardToken,omitempty"`
    BankAccountID *string `json:"bankAccountId,omitempty"`
    WalletID      *string `json:"walletId,omitempty"`
}

func (PaymentMethod) LHStructDef() littlehorse.LHStructDefInfo {
    return littlehorse.LHStructDefInfo{
        OneOf: &littlehorse.LHStructOneOfInfo{SelectionRequired: true},
    }
}

type Payment struct {
    OrderID string        `json:"orderId"`
    Method  PaymentMethod `json:"method"`
}

func (Payment) LHStructDef() littlehorse.LHStructDefInfo {
    return littlehorse.LHStructDefInfo{Name: "payment"}
}
```

With `OneOf` set, `Name` must be empty and the SDK will build an inline schema. If `OneOf` is nil, exported fields can still declare named groups using `lhoneof` tags. With neither form, the type remains an ordinary Struct. Alternatives in either form must be pointers. Nil leaves a field unset; pointers to zero or empty values still count as selections. Serialization will inspect pointer presence independently of JSON `omitempty`.

In **JavaScript / TypeScript**, we will add an `lhOneOf` helper that follows the existing [`lhStruct` API](../sdk-js/src/worker/zodSchema.ts):

```typescript
import { lhStruct, lhOneOf } from 'littlehorse-client'
import { z } from 'zod'

const PaymentMethod = lhOneOf(z.object({
  cardToken: z.string(),
  bankAccountId: z.string(),
  walletId: z.string(),
}), { selectionRequired: true })

const Payment = lhStruct('payment', z.object({
  orderId: z.string(),
  method: PaymentMethod,
}))

const payment = Payment.parse({
  orderId: 'order-123', method: { cardToken: 'tok_abc' },
})
```

`lhOneOf` will define an inline schema, make individual fields optional and check how many are populated when parsing or converting the object. It will return a new schema, preserve masking, and reject defaulted or nullable fields.

TypeScript will infer optional properties; the SDK will check selections at runtime, including task inputs and results. Plain JavaScript will use the same API.

#### Required Selections and Unset Fields

- **Group members:** At least one field must remain after ignored fields are removed. Alternatives can use any supported Struct field type, including a Struct such as `card` containing a token and billing address.
- **Field metadata:** Existing naming, descriptions, masking, and serialized-name overrides still apply. Duplicate serialized names are rejected; flat properties must have unique names before grouping.
- **Required selection:** `required` defaults to false, allowing zero or one populated alternative. Setting it to true requires exactly one. All members of a field group must agree; conflicting settings fail metadata generation.
- **Empty selection:** Flat groups always emit their container, so an unselected optional group becomes `"method": {}`. The equivalent nested model supplies an empty `PaymentMethod`.
- **Required parent:** A required `method` field must contain a Struct even when its selection is optional; `method: {}` is valid with `required = false`.
- **Optional parent:** Only the nested API can represent an absent or null parent. Existing defaults and nullability rules apply. When a non-null parent is provided, it must satisfy the oneof's selection rule.

SDKs will omit unset alternatives when serializing, and restore them when deserializing:

| SDK | Unselected alternative |
|-----|------------------------|
| Java / .NET | `null` |
| Python | `None`; generated constructors allow omitted arguments. |
| Go | Nil pointer |
| JavaScript | Missing property or `undefined`; local `null` is invalid. |

Java alternatives must use reference types, such as `Integer` rather than `int`, so null can represent an unset field.

- **Defaults and nullability:** Alternatives cannot have defaults or be marked nullable in the Struct schema. A default could populate a field the caller did not select.
- **Populated fields:** Unused alternatives must be absent from protobuf values; explicit nulls are rejected. Empty strings, empty collections, `0`, and `false` count as populated fields.
- **Errors:** Validation errors identify the affected group or field. Choice Structs reject unknown fields; ordinary Structs keep their existing handling of extra fields.
- **Group names:** Names are case-sensitive, follow Struct field naming rules, and cannot collide with serialized properties.
- **Typos:** A misspelled name can silently create another group, including a single-field group. Constants can help in Java, Python, .NET, and JavaScript; Go tags require literals. Helper types avoid group-name strings.
- **Combining APIs:** A whole-type marker cannot be combined with field-group markers on its own fields. A parent can still declare field groups and contain a oneof helper; each schema validates its own groups.
- **Workflow paths:** Both APIs use nested paths such as `method.cardToken`. Reading an unset alternative returns `NULL`.
- **Changing selections:** Assigning a second alternative fails and preserves the previous value. To switch selections, replace the containing Struct (`method`). Alternatives are not cleared automatically; field removal is outside scope.

### Protobuf Changes

Both APIs will generate nested choice Structs in [`type_definition.proto`](../schemas/littlehorse/type_definition.proto):

```protobuf
message InlineStructDef {
  map<string, StructFieldDef> fields = 1;
  StructOneOf oneof_config = 2;
}

message StructOneOf {
  bool selection_required = 1;
}
```

- **Defining a oneof:** An absent `oneof_config` means a normal Struct; a present message means a oneof. Within that message, `selection_required = false` allows zero or one populated field, while `true` requires exactly one.
- **Usage:** Oneofs live in inline helper schemas. A named StructDef can contain them, but cannot itself be a oneof.
- **Passing values:** Both APIs use the same nested Struct value, so either model can read values written by the other. The schema supplies the selection rule; values do not repeat it. No new `VariableValue` case is needed.

### Comparison with JSON Schema and Protobuf

Our feature selects a named field, like protobuf's `oneof`. JSON Schema's `oneOf` instead requires exactly one matching schema, which can describe more than field presence.

| Rule | LittleHorse | [JSON Schema `oneOf`](https://json-schema.org/draft/2020-12/json-schema-core#section-10.2.1.3) | [Protobuf `oneof`](https://protobuf.dev/programming-guides/proto3/#oneof) |
|------|-------------|---------------------|------------------|
| Single `oneof` option | Allowed. Empty choice schemas are rejected. | Allowed; the schema array must be nonempty. | Allowed. |
| No selection | Allowed unless `required` is true. | Requires a branch that matches the unselected state; exactly one branch must still match. | Allowed; requiring a selection needs additional validation. |
| Multiple selections | Rejected, preserving the previous value on failed updates. | Rejected if multiple branches match. Multiple populated fields alone do not necessarily cause multiple matches. | Setting another alternative clears the previous one; binary parsing keeps the last alternative encountered. |
| Alternative types | Any supported Struct field type. Arrays and maps already use `Array` / `Map` messages; no extra user-defined wrapper is needed. | Any valid schema. | No direct `repeated` or `map` members; collections must be wrapped in a message. |
| Where oneofs can be defined | On a helper type or a group of fields. A named StructDef can contain either, but cannot itself be marked as a oneof type. | On the entire document or within a nested schema. | Inside any message, including a top-level message; ordinary fields can coexist with the oneof. |
| Defaults | Prohibited on `oneof` options. | [`default` is an annotation](https://json-schema.org/understanding-json-schema/reference/annotations); validation does not populate missing values. | Reading an unset scalar returns its type's default without selecting that alternative. |

## Backwards Compatibility

- **Existing StructDefs:** Definitions without `oneof_config` behave as today. Servers and clients using oneofs need updated support; older servers will not enforce or preserve the constraints.
- **Schema updates:** We will keep the existing compatibility types. Under `FULLY_COMPATIBLE_SCHEMA_UPDATES`, adding, removing, renaming, or changing the type of an alternative, changing selection requirements, or moving fields into a oneof is incompatible and requires a new StructDef name.
- **Future evolution:** Adding options and moving fields into oneofs under the same StructDef name will be addressed in a separate proposal, including any new compatibility modes or field migrations.
- **Switching SDK APIs:** Moving between declaration styles preserves values and paths when the generated schemas and field names are identical.

## Alternatives

| Alternative | Tradeoff |
|-------------|----------|
| Support only one of the two SDK APIs. | Reduces SDK work, but forces either string groups or extra types onto users whose models fit the other approach better. |
| Put a list of alternative field names in one annotation above `Payment`. | Defines the rule once, but renaming a field also requires editing its name in the annotation. |
| Make `Payment.method` hold a `CardPayment` or `BankPayment` object through a sealed interface or native union. | The language restricts the value to one alternative, but the SDK needs subtype mapping and users need a separate class for each alternative. |
