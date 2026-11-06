# Proposal: OneOf Support in `Struct`s

- [Proposal: OneOf Support in `Struct`s](#proposal-oneof-support-in-structs)
  - [Background](#background)
  - [Proposed Solution](#proposed-solution)
    - [Client-Side (SDK) Changes](#client-side-sdk-changes)
      - [Declaring Oneofs in the SDKs](#declaring-oneofs-in-the-sdks)
    - [Selections and Unset Fields](#selections-and-unset-fields)
    - [Group Names and Scope](#group-names-and-scope)
    - [Workflow Access and Updates](#workflow-access-and-updates)
    - [Protobuf Changes](#protobuf-changes)
    - [Comparison with JSON Schema and Protobuf](#comparison-with-json-schema-and-protobuf)
  - [Backwards Compatibility](#backwards-compatibility)
  - [Alternatives](#alternatives)

Author: Jacob Snarr

## Background

A payment request can accept a card token, bank account ID, or wallet ID, but at most one. Ordinary Struct fields cannot express this: required fields demand all three, while nullable fields allow any combination. Oneofs reject multiple selections while allowing the payment method to remain undecided.

## Proposed Solution

Users can group individual fields or define a reusable oneof type. Both APIs generate equivalent schemas and values. This MVP allows zero or one selection; applications or workflows must enforce a required selection when needed.

### Client-Side (SDK) Changes

#### Declaring Oneofs in the SDKs

In **Java**, users will be able to declare a group on fields, getters, setters, or record components using `@LHStructOneOf`:

```java
@LHStructDef("payment")
public record Payment(
        String orderId,
        @LHStructOneOf(group = "method") String cardToken,
        @LHStructOneOf(group = "method") String bankAccountId,
        @LHStructOneOf(group = "method") String walletId) {}
```

Fields sharing the group name are mutually exclusive. `orderId` remains an ordinary field. Multiple groups can coexist in the same Struct, but a field can belong to only one group.

On a class or record, the annotation makes all included fields alternatives. Omit `group`; the containing property's serialized name names the choice:

```java
@LHStructOneOf
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

The SDK nests grouped properties when writing and expands them when reading. Java helpers can be public nested records, public static classes, or top-level types. POJOs require public no-arg constructors, getters, and setters; placement does not affect the wire format.

| Difference | Group declared on individual fields | Separate oneof type |
|---------------|-------------------------------------|---------------------|
| Setup | Add metadata to existing fields; no new class. | Define another class/type and reference it. |
| Field access | `payment.cardToken()` in Java. | `payment.method().cardToken()` in Java. |
| Reuse | Repeat group declarations in each model. | Reuse `PaymentMethod` across models. |
| Mistakes | String typos can create unintended groups; the group name is repeated. | Membership comes from the type's fields; no group-name strings are needed. |
| Wire paths | SDK creates `method.cardToken` from a flat Java property. | `method.cardToken` follows the model directly. |

Other SDKs support both forms using their existing metadata conventions. Field declarations are:

| SDK | Declaration on each participating field |
|-----|----------------------------------------|
| Python | `Annotated[str \| None, LHStructOneOf(group="method")]`, alongside existing `LHStructField` metadata. |
| .NET | `[LHStructOneOf("method")]` on a property or accessor. |
| Go | `lhoneof:"method"` on an exported pointer field, alongside its `lh` / `json` tags. |
| JavaScript / TypeScript | `lhOneOfField(z.string(), { group: 'method' })` on a field in `lhStruct`'s Zod object. |

JavaScript's `lhOneOfField` makes the field optional and preserves its metadata. The containing `lhStruct` validates groups during parsing and conversion; `lhOneOf` declares a separate helper type.

For a separate type, **Python** will use `@lh_oneof`, following the existing [`@lh_struct_def`](../sdk-python/littlehorse/lh_struct.py) conventions:

```python
from littlehorse.lh_struct import lh_struct_def, lh_oneof

@lh_oneof
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

In **.NET**, a separate inline type will use `[LHStructOneOf]`, following the existing [attribute conventions](../sdk-dotnet/LittleHorse.Sdk/Worker/LHStructDefAttribute.cs):

```csharp
[LHStructOneOf]
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

In **Go**, we will add an `IsOneOf bool` marker to [`LHStructDefInfo`](../sdk-go/littlehorse/lh_struct_def.go):

```go
type PaymentMethod struct {
    CardToken     *string `json:"cardToken,omitempty"`
    BankAccountID *string `json:"bankAccountId,omitempty"`
    WalletID      *string `json:"walletId,omitempty"`
}

func (PaymentMethod) LHStructDef() littlehorse.LHStructDefInfo {
    return littlehorse.LHStructDefInfo{
        IsOneOf: true,
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

A helper type sets `IsOneOf: true` and leaves `Name` empty because it is embedded, not registered. To group fields in a named Struct instead, tag each participating field with `lhoneof:"method"`. A type with neither marker is an ordinary Struct. Field names still come from `json` tags; `lh` tags remain optional. Alternatives must be pointers. Serialization checks pointer presence independently of JSON `omitempty`, so pointers to zero or empty values count as selections.

In **JavaScript / TypeScript**, we will add an `lhOneOf` helper that follows the existing [`lhStruct` API](../sdk-js/src/worker/zodSchema.ts):

```typescript
import { lhStruct, lhOneOf } from 'littlehorse-client'
import { z } from 'zod'

const PaymentMethod = lhOneOf(z.object({
  cardToken: z.string(),
  bankAccountId: z.string(),
  walletId: z.string(),
}))

const Payment = lhStruct('payment', z.object({
  orderId: z.string(),
  method: PaymentMethod,
}))

const payment = Payment.parse({
  orderId: 'order-123', method: { cardToken: 'tok_abc' },
})
```

Alternatives are declared with their value schemas, such as `z.string()`, but `lhOneOf` makes each one optional. Its inferred type is `{ cardToken?: string; bankAccountId?: string; walletId?: string }`, so the example above type-checks, and `parse` rejects objects with more than one alternative set.

### Selections and Unset Fields

Every oneof allows zero or one populated alternative, with no required-selection setting. These empty selections in the two Java models both serialize to `{"orderId": "order-123", "method": {}}`:

```java
// Field-group model:
new Payment("order-123", null, null, null);

// Helper-type model:
new Payment("order-123", new PaymentMethod(null, null, null));
```

A required parent field must contain a Struct, but `{}` is valid. Only the helper-type API can represent an absent or null parent, following existing defaults and nullability rules. A non-null parent must satisfy the choice constraint.

SDKs omit unselected alternatives on the wire and restore them when reading:

| SDK | Unselected alternative |
|-----|------------------------|
| Java / .NET | `null` |
| Python | `None`; constructors allow omitted arguments. |
| Go | Nil pointer |
| JavaScript | Missing property or `undefined`; local `null` is invalid. |

Alternatives support existing Struct field types, including native arrays, maps, and nested Structs; existing restrictions such as the ban on `JSON_OBJ` and `JSON_ARR` still apply. Java alternatives must be reference types, such as `Integer` rather than `int`.

Alternatives cannot have defaults or schema-level nullability. Explicit wire nulls are rejected; empty strings, empty collections, `0`, and `false` count as selections. Choice Structs reject unknown fields; ordinary Structs retain their existing handling of extra fields.

### Group Names and Scope

**Naming**: Names are case-sensitive and follow Struct field naming rules. A field group uses its `group` name; a helper uses its containing property's serialized name, not its class name. Competing declarations for the same serialized field are rejected, not merged. Errors identify the containing type, conflicting property, and remedy, for example:

> Cannot create oneof group 'method' in Payment: it would create a nested field already used by property 'method'. Rename the group or property's serialized field, or use one declaration for this choice.

**Name locality and scope**: Names are local to each Struct: `method` and `child.method` are independent and may both have selections. Java inherited and subclass properties share one Struct; matching group names combine into one group with at most one selection. Typos can create unintended groups, including single-field groups; constants or helper types reduce this risk.

**Mixing OneOf strategies**: A parent may mix field groups and helper properties. A whole-type marker cannot be combined with field-group markers on that type's own fields. Helpers are embedded and cannot have their own StructDef names in any SDK: Java's type-level `@LHStructOneOf` cannot accompany `@LHStructDef`. Named StructDefs contain choices so they can later gain ordinary fields alongside them.

### Workflow Access and Updates

Both APIs use nested paths such as `method.cardToken`; reading an unset alternative returns `NULL`. Assigning a second alternative, whether from a literal, task output, or `ExternalEvent`, fails the `NodeRun` with `VAR_SUB_ERROR`, like other failed variable mutations. All mutations on that edge roll back, preserving the previous value. Switch selections by replacing the containing Struct (`method`). Alternatives are not cleared automatically; field removal is outside scope.

### Protobuf Changes

Both APIs will generate nested choice Structs in [`type_definition.proto`](../schemas/littlehorse/type_definition.proto):

```protobuf
message InlineStructDef {
  map<string, StructFieldDef> fields = 1;
  bool is_oneof = 2;
}
```

`is_oneof = true` marks a choice; false or omitted retains ordinary Struct rules.

Future options could use a `StructOneOf` configuration message in a new protobuf field. Field 2 must retain its boolean type for wire compatibility.

### Comparison with JSON Schema and Protobuf

Our feature selects a named field, like protobuf's `oneof`. JSON Schema's `oneOf` instead requires exactly one matching schema, which can describe more than field presence.

| Rule | LittleHorse | [JSON Schema `oneOf`](https://json-schema.org/draft/2020-12/json-schema-core#section-10.2.1.3) | [Protobuf `oneof`](https://protobuf.dev/programming-guides/proto3/#oneof) |
|------|-------------|---------------------|------------------|
| Single `oneof` option | Allowed. Empty choice schemas are rejected. | Allowed; the schema array must be nonempty. | Allowed. |
| No selection | Allowed. Requiring a selection is outside this MVP. | Requires a branch that matches the unselected state; exactly one branch must still match. | Allowed; requiring a selection needs additional validation. |
| Multiple selections | Rejected, preserving the previous value on failed updates. | Rejected if multiple branches match. Multiple populated fields alone do not necessarily cause multiple matches. | Setting another alternative clears the previous one; binary parsing keeps the last alternative encountered. |
| Alternative types | Any supported Struct field type. Arrays and maps already use `Array` / `Map` messages; no extra user-defined wrapper is needed. | Any valid schema. | No direct `repeated` or `map` members; collections must be wrapped in a message. |
| Where oneofs can be defined | On a helper type or a group of fields. A named StructDef can contain either, but cannot itself be marked as a oneof type. | On the entire document or within a nested schema. | Inside any message, including a top-level message; ordinary fields can coexist with the oneof. |
| Defaults | Prohibited on `oneof` options. | [`default` is an annotation](https://json-schema.org/understanding-json-schema/reference/annotations); validation does not populate missing values. | Reading an unset scalar returns its type's default without selecting that alternative. |

JSON Schema can express this constraint with optional, non-null alternative properties, `additionalProperties: false`, and `maxProperties: 1`. An empty object is valid. Requiring a selection would add `minProperties: 1`. These [object constraints](https://json-schema.org/understanding-json-schema/reference/object#size) need no LittleHorse selection configuration; importing required selections or arbitrary JSON Schema `oneOf` expressions is outside scope.

## Backwards Compatibility

- **Existing StructDefs:** Definitions with `is_oneof` omitted or false behave as today. Servers and clients using oneofs need updated support; older servers will not enforce or preserve the constraints.
- **Schema updates:** We will keep the existing compatibility types. Under `FULLY_COMPATIBLE_SCHEMA_UPDATES`, adding, removing, renaming, or changing the type of an alternative, changing `is_oneof`, or moving fields into a oneof is incompatible and requires a new StructDef name.
- **Future evolution:** Adding options and moving fields into oneofs under the same StructDef name will be addressed in a separate proposal, including any new compatibility modes or field migrations.
- **Switching SDK APIs:** Moving between declaration styles preserves values and paths when the generated schemas and field names are identical.

## Alternatives

| Alternative | Tradeoff |
|-------------|----------|
| Support only one of the two SDK APIs. | Reduces SDK work, but forces either string groups or extra types onto users whose models fit the other approach better. |
| Put a list of alternative field names in one annotation above `Payment`. | Defines the rule once, but renaming a field also requires editing its name in the annotation. |
| Make `Payment.method` hold a `CardPayment` or `BankPayment` object through a sealed interface or native union. | The language restricts the value to one alternative, but the SDK needs subtype mapping and users need a separate class for each alternative. |
| Add a required-selection setting. | Useful for enforcing business rules such as a payment requiring a method, but adds SDK configuration and validation semantics. Deferred beyond the MVP; all choices initially allow zero or one selection. |
