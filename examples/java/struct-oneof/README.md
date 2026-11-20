## Running Struct OneOf Example

A oneof group makes `Struct` fields mutually exclusive. Here, a `Customer` can be reached by `email` **or** `phoneNumber`, never both:

```java
@LHStructDef("customer")
public class Customer {
    private String name;

    @LHStructOneOf(group = "contact")
    private String email;

    @LHStructOneOf(group = "contact")
    private String phoneNumber;
}
```

The `notify-customer` task uses whichever contact method is set.

### Run the example

```bash
./gradlew example-struct-oneof:run
```

This registers the `StructDef`, `TaskDef`, and `WfSpec`, starts the worker, and runs the workflow twice: once with an `email` and once with a `phoneNumber`. Inspect a run with:

```bash
lhctl get wfRun <wf_run_id>
lhctl list nodeRun <wf_run_id>
```

Setting both `email` and `phoneNumber` is rejected, since only one field in the `contact` group may be set.
