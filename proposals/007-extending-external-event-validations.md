# Allowing `PutExternalEventDef` to fail if the corresponding WfRun does not exist yet

## Motivation
The current implementation of an `externalEvent` allows exterior events to affect interior logic, or in other words how we proceed within a `wfRun`. There exist a few guardrails and specifications that allow devs to have more control over how and when an `externalEvent` can affect a `wfRun`.

They are the following:

1) Retention Policy: Clean/Delete an `externalEvent` if not claimed by a `wfRun` after x amount of time

2) Specify the `threadRun` as well as the `nodeRun` that an `externalEvent` is sent to

The following proposal aims to extend upon these guardrails and specifications to create a better experience for a `wfSpec` developer


## Proposal
### Preemptive check to validate wfRun exists

As of right now in littlehorse if you want to set a deadline for an external events lifespan the way of doing so is via setting a retention policy in the `putExternalEventDefRequest`. This allows external events to be cleaned up after x amount of time.

However there is no explicit functionality to say that a WfRun must exist when an external event is posted to the littlehorse server.

Take into account a workflow that uses a docusign document as a point of authorization to ship a premium item.

The workflow may look something like this:

(Premium item ordered) -> (check stock) -> (validate credit history) ->(create document) -> (sign off)
```java
WfRunVariable item = wf.declareStr("item").required()
WfRunVariable customer = wf.declareJsonObj("customer")

wf.execute("check-stock", item)
wf.execute("validate-customer")
wf.execute("create-document", item, customer)
wf.waitForEvent("document-signed")
```

In this simple workflow there would be no reason for an External Event to be posted prior to a `WfRun`. Therefore the best practice would be to add a preemptive check to confirm that the `WfRun` exists when the external event is posted.

For further isolation of when the external event can be posted `putExternalEventDefRequest` already has two optional fields to specify the `threadRun` as well as `nodeRun` at which the external event can occur.

Adding a flag that enforces whether or not an external event can be pre-posted prior to `WfRun` will provide the full scope of granularity needed to give developers full control over how and when `externalEvents` can affect a workflow.

### Unreferenced External Events

Currently in littlehorse if an `externalEvent` is posted to a `WfRun` with a `WfSpec` that has no reference to that event then the external event will wait around to be cleaned up.

This can be avoided by adding another optional flag as well in `putExternalEventDefRequest` that represents whether an event must be referenced in the corresponding `wfSpec` of the `WfRun` it is posted to.


### Protobuf changes

```proto
// Field to create an ExternalEventDef.
message PutExternalEventDefRequest {

  // ...

  // Flag to represent whether or not a wfRun must exist
  // for a external event to advance workflow or trigger and interrupt
  optional boolean require_wfrun  = 4;

  // Flag that represents whether or not the corresponding 
  // WfSpec must reference the external event being posted
  optional boolean  require_wfspec_reference = 5;

}
```
### Compatibility

Keep in mind that the current default functionality among all clients is that a premature external event can trigger advances and interrupts. Also we do not check to see if a `wfSpec` references an external event giving events the ability to hang.

In order to provide backwards compatibility the default values must not require a `wfRun` to exist, as well as not require a `wfSpec` to hold reference to an external event.

I chose flags over enums to represent these two functionalities because I believe that they are two separate binary operations where extensibility is not necessary.





 

