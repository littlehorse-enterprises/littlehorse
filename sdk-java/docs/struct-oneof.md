# Mutually exclusive Struct fields

`@LHStructOneOf` groups Struct fields so that zero or one alternative may be selected.
Use reference types: `null` means unselected, while `false`, `0`, empty strings, and
empty collections count as selections.

```java
@LHStructDef("payment")
public record Payment(
        String orderId,
        @LHStructOneOf(group = "method") String card,
        @LHStructOneOf(group = "method") String bank) {}
```

Alternatively, reuse a helper type. Its fields are flattened into the parent; the
containing property's serialized name becomes the group name.

```java
@LHStructOneOf
public record Method(String card, String bank) {}

@LHStructDef("payment")
public record Payment(String orderId, Method method) {}
```

Both forms generate the same schema and wire fields. A selected card uses the
workflow path `card`, not `method.card`. A null helper writes an empty selection;
reading it restores `new Method(null, null)`. Marking the helper property with
`@LHStructField(masked = true)` masks every flattened alternative.

Field annotations may be placed on fields, getters, setters, or record components.
Inherited properties share groups. POJO helpers must be public, have public no-arg
constructors, and expose getters and setters. Helpers cannot also have `@LHStructDef`,
a registered type adapter, or declare groups on their own fields. Serialized field
collisions are rejected.

Alternatives cannot have defaults or `@LHStructField(isNullable = true)`.
Explicit null fields on the wire and simultaneous selections are rejected on both
serialization and deserialization. Omitted alternatives read as Java nulls.
Supported field types include native arrays, maps, and nested Structs.

In a workflow, assigning `NULL` to an alternative unsets it. Mutations on an edge are
applied together and only the final value is validated, so switching selections takes
two mutations on the same edge in either order:

```java
payment.get("card").assign(null);
payment.get("bank").assign("acct_123");
```

If an edge leaves two alternatives selected, the `NodeRun` fails with `VAR_SUB_ERROR`
and every mutation on that edge rolls back. Replacing the entire Struct is another way
to switch selections. A Struct builder that sets two alternatives in one group is
rejected when the `WfSpec` is registered.

Adding a group to an existing StructDef is compatible only for new fields. Moving an
existing field into a group, or removing an alternative, requires a new StructDef name.

Use a server with flat OneOf support. Older servers ignore the group metadata and
treat alternatives as ordinary required fields.
