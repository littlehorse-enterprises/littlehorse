# Correlated `ExternalEvent`s

## Motivation

Users have complained that sometimes when posting ExternalEvents it is hard to know what the WfRunId should be, and that sometimes a single event should go to mulltiple WfRuns. Advanced users have requested functionality like the following:

```java
WfRunVariable documentId = wf.declareStr("docusign-id");
documentId.assign(wf.execute("create-docusign"));
wf.waitForevent("document-signed").withCorrelationId(documentId).timeoutSeconds(60 * 60 * 24 * 7);
```

Then instead of posting an ExternalEvent in which they need to know the WfRunId that they are signaling, they can just post a CorrelatedEvent (a new Getable that we introduce) with the document ID as a correlation.

```java
String documentId = "asdfapoghweofjadksla"; // whatever docusign's webhook gives you

client.putCorrelatedEvent(PutCorrelatedEventRequest.newBuilder()
                .setExternalEventDefId(ExternalEventDefId.newBuilder().setName("document-signed"))
                .setContent(LHLibUtil.objToVarVal("document was signed!"))
                .setKey(documentId)
                .build());
```

Separately, other users have requested the ability to put, search, and retrieve records (arbitrary pieces of data) into LittleHorse, as if to use 
LH as a lightweight database. As we will see soon, both motivations can be completed with one really nice design.


## Proposal

* **`ExternalEvent`**: it's like a signal to a specific `WfRun` saying something happened _outside that `WfRun`_. Already exists. I wish we could rename it to `Signal` but 1. Temporal does that and 2. it would be a breaking change.
* **`CorrelatedEvent`**: represents a piece of data from outside the LH Server. It can spawn off zero or more `ExternalEvent`s.

A `CorrelatedEvent` is associated with an `ExternalEventDef`.

### Protobuf

```proto
// The ExternalEventDef defines the blueprint for an ExternalEvent.
message ExternalEventDef {
  // The id of the ExternalEventDef.
  ExternalEventDefId id = 1;

  // When the ExternalEventDef was created.
  google.protobuf.Timestamp created_at = 2;

  // The retention policy for ExternalEvent's of this ExternalEventDef. This applies to the
  // ExternalEvent **only before** it is matched with a WfRun.
  ExternalEventRetentionPolicy retention_policy = 3;

  // Schema that validates the content of any ExternalEvent's posted for this ExternalEventDef.
  //
  // It is _optional_ for compatibility purposes: ExternalEventDef's that were created
  // before 0.13.2 will not have a schema. For those `ExternalEventDef`s that do not have
  // a specified type_information, we do not validate the WfSpec's usage of the ExternalEvent
  // nor do we validate the type of `content` in the `rpc PutExternalEvent`.
  optional ReturnType type_information = 4;

  // If not set, then the users cannot use the `rpc PutCorrelatedEvent` to post externalEvents of this
  // type.
  optional CorrelatedEventConfig correlated_event_config = 5;
}

// Configures behavior of `CorrelatedEvent`s created for a specific `ExternalEventDef`.
message CorrelatedEventConfig {
  // If ttl_seconds is set, then `CorrelatedEvent`s will be automatically
  // cleaned up based on the provided ttl.
  optional int64 ttl_seconds = 1;

  // If true, delete the `CorrelatedEvent` after the first `ExternalEvent` is created.
  // Also, if set, it is implied that only one `WfRun` can ever be correlated
  // to this `CorrelatedEvent`.
  bool delete_after_first_correlation = 2;
}

// The ID of a CorrelatedEvent, which is a precursor to an `ExternalEvent`.
message CorrelatedEventId {
  // The key used for correlation
  string key = 1;

  // The `ExternalEventDefId`
  ExternalEventDefId external_event_def_id = 2;
}

// A CorrelatedEvent is a piece of data that has been posted into LittleHorse but is not
// yet associated with any specific `WfRun`. This allows users to indirectly create
// `ExternalEvent`s without knowing the `WfRunId` that they are posting the
// `ExternalEvent` to by taking advantage of the correlation id feature of a
// `CorrelatedEvent`.
//
// CorrelatedEvents also serve as a way to simply store data in LittleHorse.
message CorrelatedEvent {
  // The ID of the CorrelatedEvent
  CorrelatedEventId id = 1;

  // The time at which the `CorrelatedEvent` was created.
  google.protobuf.Timestamp created_at = 2;

  // The content of the `CorrelatedEvent`.
  VariableValue content = 3;

  // List of `ExternalEvent`s that have been created for this `CorrelatedEvent`.
  repeated ExternalEventId external_events = 4;
}
```

The `ExternalEventNodeRun` will be modified as follows:

```proto
// The sub-node structure for an EXTERNAL_EVENT NodeRun.
message ExternalEventNodeRun {
  // ...

  // Correlation ID for the External Event Node to allow posting events by
  // correlation (without knowing the WfRunId in advance). If not set,
  // the ExternalEvent poster must know the WfRunId.
  optional string correlation_key = 5;
}
```

And some new RPC's:

```proto
// Request used to create a `CorrelatedEvent` or update its content.
message PutCorrelatedEventRequest {
  // The correlation key of the CorrelatedEvent.
  string key = 1;

  // The `ExternalEventDef` that is associated with this `CorrelatedEvent`. This is
  // also the `ExternalEventDef` of any `ExternalEvent`s that are generated after
  // this `CorrelatedEvent` is correlated to `WfRun`s.
  ExternalEventDefId external_event_def_id = 2;

  // The content of the CorrelatedEvent and any `ExternalEvent`s created after
  // correlating this `CorrelatedEvent`.
  VariableValue content = 3;
}

message SearchCorrelatedEventRequest {
  // ExternalEventDefId of CorrelatedEvents to search for
  ExternalEventDefId external_event_def_id = 1;

  // earliest create time
  optional google.protobuf.Timestamp earliest = 2;

  // latest create time
  optional google.protobuf.Timestamp latest = 3;

  // If set, returns only CorrelatedEvent's that have or do not have correlated
  // `ExternalEvent`s. Useful for finding orphaned `CorrelatedEvent`s.
  optional bool has_correlated_events = 4;

  // Maximum number of results
  int32 limit = 5;

  // Bookmark
  optional bytes bookmark = 6;
}

service LittleHorse {
  // ...

  // Put a CorrelatedEvent in LittleHorse. If there are any `ExternalEventNodeRun`s waiting
  // for a CorrelatedEvent with the same correlation ID, then one or more `ExternalEvent`s
  // will be created.
  rpc PutCorrelatedEvent(PutCorrelatedEventRequest) returns (CorrelatedEvent) {}

  // Get a specific CorrelatedEvent.
  rpc GetCorrelatedEvent(CorrelatedEventId) returns (CorrelatedEvent) {}

  // Deletes a CorrelatedEvent.
  rpc DeleteCorrelatedEvent(DeleteCorrelatedEventRequest) returns (google.protobuf.Empty) {}
}
```

## Implementation

The `CorrelatedEvent` is partitioned by its `id.key`.

### Storage

We'll create a _private_ `Storeable`:

```proto
message CorrelationMarker {
  // NodeRunId of the `ExternalEventNodeRun` that's waiting
  NodeRunId source_node_run = 1;

  // ExternalEventDefId that we're waiting for
  ExternalEventDefId event_def_id = 2;

  // Correlation key
  string correlation_key = 3;

  // When we started waiting
  google.protobuf.Timestamp created_at = 4;
}
```

When you arrive at an `ExternalEventNode` with the `correlation_key` set, it will create a `CorrelationMarker` which gets sent (repartitioned) to the partition where the `CorrelatedEvent` would live.

### `ExternalEventNode` Timeout

When the `ExternalEventNodeRun` times out, we will utilize the Timer topology to delete the associated `CorrelationMarker`.

### Using the Timer / Boomerang Topology

We can't allow the Core Topology to produce records that end up on the Core Command topic. Why?

1. It's an EOS topology and transactional messages in the core command topic have really bad latency implications.
2. There were historically some VERY bad Kafka Streams bugs when you had EOS topologies with loops (a sink produced to an input topic). I'm scared to check if they werer fixed.

As a solution, when the Core processor needs to send a `Command` to another partition on the Core topology, we will send a `LHTimer` with timestmamp zero to the Timer topology, which will then "boomerang" back to the correct partition in the Core Topology.

We do this in two places:

1. When creating a `CorrelationMarker`
2. When a `Correlationmarker` matches with a `CorrelatedEvent` and we need to create an `ExternalEvent`.

## Rejected Alternatives

These aren't the droids we're looking for.

### Modify `rpc PutExternalEvent`

We can't modify `rpc PutExternalEvent` to take in a `correlation_key` because:

* An `ExternalEventId` requires a `WfRunId`
* Therefore you cannot have an `ExternalEvent` without a `WfRunId`
* We don't have a `WfRunId`, so we can't create (nor return) an `ExternalEvent`
* It's also possible to have one "event" create multiple `ExternalEvent`s

### Make `CorrelatedEvent` Not A `Getable`

Problems with this:

* How do we view "pending external events that aren't correlated yet"?
* How do we delete them?

Basically, the administrator / operational UX would be pretty bad. And we get the added benefit here of allowing (advanced) users to use LH as a storage center for small amounts of data.

### Make `CorrelatedEventDef`

Coupling `CorrelatedEvent` to `ExternalEventDef` makes the relationship very clear. Otherwise it gets really hairy, especially with strong typing.
