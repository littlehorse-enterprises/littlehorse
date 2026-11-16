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

Users can group individual fields or define a reusable oneof type. Both APIs generate equivalent schemas and values. Alternatives remain ordinary fields of their Struct; a group is a constraint that at most one of its fields is set. This MVP allows zero or one selection; applications or workflows must enforce a required selection when needed.

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

On a class or record, the annotation makes all included fields alternatives. Omit `group`; the containing property's serialized name names the group:

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
  "cardToken": "tok_abc"
}
```

The SDK flattens a helper's fields into its parent when writing and regroups them when reading; the helper property itself has no wire field. Java helpers can be public nested records, public static classes, or top-level types. POJOs require public no-arg constructors, getters, and setters; placement does not affect the wire format.

| Difference | Group declared on individual fields | Separate oneof type |
|---------------|-------------------------------------|---------------------|
| Setup | Add metadata to existing fields; no new class. | Define another class/type and reference it. |
| Field access | `payment.cardToken()` in Java. | `payment.method().cardToken()` in Java. |
| Reuse | Repeat group declarations in each model. | Reuse `PaymentMethod` across models, at most once per Struct. |
| Mistakes | String typos can create unintended groups; the group name is repeated. | Membership comes from the type's fields; no group-name strings are needed. |
| Wire paths | `cardToken` follows the model directly. | SDK creates `cardToken` from a nested Java property. |

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

`lh_oneof` will register the helper's fields as alternatives in the parent and generate a constructor that initializes omitted alternatives to `None`. Field-level markers will also allow omitted alternatives.

In **.NET**, a separate type will use `[LHStructOneOf]`, following the existing [attribute conventions](../sdk-dotnet/LittleHorse.Sdk/Worker/LHStructDefAttribute.cs):

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

A helper type sets `IsOneOf: true` and leaves `Name` empty because its fields are flattened into the parent, not registered. To group fields in a named Struct instead, tag each participating field with `lhoneof:"method"`. A type with neither marker is an ordinary Struct. Field names still come from `json` tags; `lh` tags remain optional. Alternatives must be pointers. Serialization checks pointer presence independently of JSON `omitempty`, so pointers to zero or empty values count as selections.

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

Every oneof allows zero or one populated alternative, with no required-selection setting. These empty selections in the two Java models both serialize to `{"orderId": "order-123"}`:

```java
// Field-group model:
new Payment("order-123", null, null, null);

// Helper-type model:
new Payment("order-123", new PaymentMethod(null, null, null));
```

Because a group has no wire field of its own, an empty selection and a null helper are indistinguishable on the wire. Writing a null helper is equivalent to writing an empty one; reading always restores a helper instance whose alternatives are unset.

SDKs omit unselected alternatives on the wire and restore them when reading:

| SDK | Unselected alternative |
|-----|------------------------|
| Java / .NET | `null` |
| Python | `None`; constructors allow omitted arguments. |
| Go | Nil pointer |
| JavaScript | Missing property or `undefined`; local `null` is invalid. |

Alternatives support existing Struct field types, including native arrays, maps, and nested Structs; existing restrictions such as the ban on `JSON_OBJ` and `JSON_ARR` still apply. Java alternatives must be reference types, such as `Integer` rather than `int`.

Alternatives cannot have defaults or schema-level nullability, and are never required, even though they have no default. Explicit wire nulls are rejected; empty strings, empty collections, `0`, and `false` count as selections. Ordinary fields in the same Struct keep their existing rules, including the handling of extra fields.

### Group Names and Scope

**Naming**: Names are case-sensitive and follow Struct field naming rules. A field group uses its `group` name; a helper uses its containing property's serialized name, not its class name. Group names are a separate namespace from field names, so a group may share a name with a field. Because helpers are flattened, their fields share the parent's field namespace. Field name collisions are rejected, not merged. Errors identify the containing type, conflicting property, and remedy, for example:

> Cannot flatten oneof helper 'backupMethod' into Payment: field 'cardToken' is already declared by property 'method'. Rename one of the serialized fields, or use field groups for this choice.

**Name locality and scope**: Groups are local to each Struct: `method` and `child.method` are independent and may both have selections. Java inherited and subclass properties share one Struct; matching group names combine into one group with at most one selection. Typos can create unintended groups, including single-field groups; constants or helper types reduce this risk.

**Mixing OneOf strategies**: A parent may mix field groups and helper properties. A whole-type marker cannot be combined with field-group markers on that type's own fields. Helpers are flattened and cannot have their own StructDef names in any SDK: Java's type-level `@LHStructOneOf` cannot accompany `@LHStructDef`. A named StructDef may consist entirely of alternatives and can later gain ordinary fields alongside them.

### Workflow Access and Updates

Alternatives keep their ordinary paths such as `cardToken`; reading an unset alternative returns `NULL`. In a workflow, assigning `NULL` to an alternative unsets it. Each mutation is checked against the group as soon as it is applied, and an edge's mutations run in order, so switching selections takes two ordered mutations on the same edge: first assign `NULL` to `cardToken`, then assign `walletId`. Replacing the whole Struct also works.

Assigning a second alternative while another is still set, whether from a literal, task output, or `ExternalEvent`, fails the `NodeRun` with `VAR_SUB_ERROR`, like other failed variable mutations. All mutations on that edge roll back, preserving the previous value. Alternatives are not cleared automatically.

### Protobuf Changes

Both APIs will mark alternatives on [`StructFieldDef`](../schemas/littlehorse/type_definition.proto):

```protobuf
message StructFieldDef {
  TypeDefinition field_type = 1;
  optional VariableValue default_value = 2;
  bool is_nullable = 3;
  optional string description = 4;
  optional string oneof_group = 5;
}
```

Fields in the same `InlineStructDef` with the same `oneof_group` form one group. An omitted `oneof_group` retains ordinary field rules. Because membership is stored on each field, renaming or removing a field cannot leave a group referencing a missing field, and a field can belong to at most one group.

Future options, such as a required selection, could use a `map<string, StructOneOfGroup>` keyed by group name in a new `InlineStructDef` field. Field 5 must retain its string type for wire compatibility.

### Comparison with JSON Schema and Protobuf

Our feature selects a named field, like protobuf's `oneof`. JSON Schema's `oneOf` instead requires exactly one matching schema, which can describe more than field presence.

| Rule | LittleHorse | [JSON Schema `oneOf`](https://json-schema.org/draft/2020-12/json-schema-core#section-10.2.1.3) | [Protobuf `oneof`](https://protobuf.dev/programming-guides/proto3/#oneof) |
|------|-------------|---------------------|------------------|
| Single `oneof` option | Allowed. | Allowed; the schema array must be nonempty. | Allowed. |
| No selection | Allowed. Requiring a selection is outside this MVP. | Requires a branch that matches the unselected state; exactly one branch must still match. | Allowed; requiring a selection needs additional validation. |
| Multiple selections | Rejected when the second alternative is assigned, preserving the previous value on failed updates. | Rejected if multiple branches match. Multiple populated fields alone do not necessarily cause multiple matches. | Setting another alternative clears the previous one; binary parsing keeps the last alternative encountered. |
| Alternative types | Any supported Struct field type. Arrays and maps already use `Array` / `Map` messages; no extra user-defined wrapper is needed. | Any valid schema. | No direct `repeated` or `map` members; collections must be wrapped in a message. |
| Where oneofs can be defined | On a group of fields in any Struct, including a named StructDef. Helper types are flattened into their parent. | On the entire document or within a nested schema. | Inside any message, including a top-level message; ordinary fields can coexist with the oneof. |
| Field paths | Unchanged; alternatives stay at the Struct's top level. | Unchanged; `oneOf` constrains existing properties. | Unchanged; oneof members are ordinary fields on the wire. |
| Defaults | Prohibited on `oneof` options. | [`default` is an annotation](https://json-schema.org/understanding-json-schema/reference/annotations); validation does not populate missing values. | Reading an unset scalar returns its type's default without selecting that alternative. |

JSON Schema can express this constraint with [`dependentSchemas`](https://json-schema.org/understanding-json-schema/reference/conditionals#dependentSchemas): each alternative property forbids the others, for example `"cardToken": {"not": {"anyOf": [{"required": ["bankAccountId"]}, {"required": ["walletId"]}]}}`. An object with none of them is valid. Requiring a selection would add an `anyOf` of `required` clauses. Importing required selections or arbitrary JSON Schema `oneOf` expressions is outside scope.

## Backwards Compatibility

- **Existing StructDefs:** Fields with `oneof_group` omitted behave as today. Servers and clients using oneofs need updated support. Older servers ignore `oneof_group` and treat alternatives as required fields without defaults, so values with unselected alternatives fail validation rather than being silently accepted.
- **Schema updates:** We will keep the existing compatibility types. Under `FULLY_COMPATIBLE_SCHEMA_UPDATES`:
  - Adding a new group, or adding a new field to an existing group, is compatible, since alternatives are never required.
  - Moving one existing optional field into a group is compatible on the wire, since no stored value can have more than one alternative set. It still changes behavior when the field had a default: alternatives cannot have defaults, so the field loses it. The server materializes defaults when values are written, so stored values keep the old default and count as having that alternative selected, while new values leave it unset.
  - Moving more than one existing field into a group, moving a required field into a group, removing a field from a group, or changing an alternative's type is incompatible and requires a new StructDef name. The server cannot verify that stored values set at most one of several existing fields.
- **Future evolution:** Moving several existing fields into one group under the same StructDef name will be addressed in a separate proposal, including any data checks or new compatibility modes.
- **Switching SDK APIs:** Moving between declaration styles preserves values and paths when the generated schemas and field names are identical.

## Alternatives

| Alternative | Tradeoff |
|-------------|----------|
| Support only one of the two SDK APIs. | Reduces SDK work, but forces either string groups or extra types onto users whose models fit the other approach better. |
| Put a list of alternative field names in one annotation above `Payment`. | Defines the rule once, but renaming a field also requires editing its name in the annotation. |
| Nest each choice under its own Struct marked as a oneof. | A choice becomes a single value that can be passed, null-checked, or replaced in one assignment, and a helper type can appear more than once per Struct. However, paths change from `cardToken` to `method.cardToken`, moving existing fields into a oneof is incompatible, and SDKs must nest and expand field groups. |
| Clear the previous alternative automatically when another is assigned, like protobuf. | Switching selections takes one mutation, but a mistaken assignment silently discards data. |
| Store groups as a list of field names on `InlineStructDef`. | Keeps group configuration in one place, but renaming or removing a field can leave a dangling reference that the server must reject. |
| Make `Payment.method` hold a `CardPayment` or `BankPayment` object through a sealed interface or native union. | The language restricts the value to one alternative, but the SDK needs subtype mapping and users need a separate class for each alternative. |
| Add a required-selection setting. | Useful for enforcing business rules such as a payment requiring a method, but adds SDK configuration and validation semantics. Deferred beyond the MVP; all choices initially allow zero or one selection. |
