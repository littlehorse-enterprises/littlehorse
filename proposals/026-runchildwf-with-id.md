# Set Child WfRunId on Node `RunWf`

Users have requested the ability to specify the `id` of the child `WfRun` when using `RunChildWfNode`. Use-cases:

* Opentelemetry tracing
* Being able to correctly infer the ID for use-cases such as sending external events

## Proposed Changes

### Protobuf

Add an `optional VariableAssignment child_id = ?;` field to `RunChildWfNode`.

### SDK

```java
wf.runWf("some-child", Map.of(...)).withChildId(someValue);
```

where `someValue` is any Serializable.

Other SDK's are similar, except that Python uses an optional parameter rather than the java-style fluent builder.

## Test Plan

* E2E test which verifies that the child WfRun exists by using `client.getWfRun()` with the specified ID.
* E2E test that verifies an ERROR (type VAR_SUB_ERROR) in case the same WfRun starts two child WfRuns with the same id.
* Unit tests in python, go, c#