# The Output Topic

- [The Output Topic](#the-output-topic)
  - [Motivation](#motivation)
    - [Workflows as Tables](#workflows-as-tables)
    - [Workflows as Streams](#workflows-as-streams)
  - [Topic Structure](#topic-structure)
    - [Metadata and Execution Data](#metadata-and-execution-data)
    - [Multi-Tenancy](#multi-tenancy)
    - [Topic Naming](#topic-naming)
    - [Partitioning](#partitioning)
  - [Proto Schemas](#proto-schemas)
    - [Tenant](#tenant)
    - [Output Topic Schemas](#output-topic-schemas)
    - [Metadata Output Topic](#metadata-output-topic)
  - [Configuring What's Sent](#configuring-whats-sent)
    - [Metadata Getable Changes](#metadata-getable-changes)
  - [Implementation](#implementation)
    - [Reads vs Puts](#reads-vs-puts)
    - [Testing](#testing)
  - [Future Work](#future-work)


## Motivation

The Output Topic will allow users of LittleHorse to export data in real-time from their LittleHorse Workflows into external systems.

On a personal note, when I started the LittleHorse Server project over three years ago, I did it with the intention of bridging the gap between Workflows, Streams, and Tables.

### Workflows as Tables
I believe that **Workflows are Data.** For example, consider the following `orders` workflow:

```java
var userId = wf.declareStr("user-id");
var itemId = wf.declareStr("item-id");
var orderStatus = wf.declareStr("order-status").withDefault("PENDING");

wf.execute("charge-credit-card", userId, wf.execute("fetch-price", itemId));
wf.execute("ship-item", userId, itemId);
orderStatus.assign("SHIPPING");

wf.waitForEvent("item-delivered");
orderStatus.assign("COMPLETED");
```

If you wanted to "export this workflow" into a database such as Postgres or Snowflake, you might create a database table that looks like the following:

```sql
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    item_id TEXT NOT NULL,
    order_status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);
```
And then insert a new row for every single `WfRun`. This would allow you to do analytics based on your orders.

This can be accomplished with an _Output Topic_ that publishes updates to `WfRun` data in real time to Apache Kafka.

### Workflows as Streams

Another motivation for the Output Topic is that updates to your `WfRun`s can be streams of events. For example, the following use-cases have come up:

* Notifications when a `TaskDef` fails five times in a minute.
* Trigger an "incident response" `WfSpec` when 10 `WfRun`s of a specific type reach a certain failure scenario in a given time window.
* Send an alert to the business team when there is an excessively long backlog of `UserTaskRun`s assigned to the same group.
* Create "cascading workflows," wherein the completion of one `WfRun` triggers another `WfRun` in a loosely-coupled manner.

The above can also be accomplished as well through Kafka.

## Topic Structure

Before we can propose the structure of the Kafka topic(s) used for the Output Topic effort, we need to take a look at how LittleHorse's data is structured.

### Metadata and Execution Data
There are two types of data in LittleHorse:
1. Metadata, such as `WfSpec`, `TaskDef`, etc.
2. Execution data, such as `WfRun`, `TaskRun`, etc.

Metadata is small, relatively static, and global to a cluster. Execution data is large, partitioned, and constantly changing. Consumers doing stream processing on Execution data will often need access to Metadata in order to properly make sense of the Execution Data.

Therefore, we will separate metadata and execution data into two topics:
1. The `metatadata-output` topic, which is a single-partition, non-compacted topic containing metadata updates.
2. The `execution-output` topic, which is a multi-partition, non-compacted topic containing execution data updates.

It is important for the metadata output topic to be single-partition so that all of the metadat `Getable`s have ordering.

At first glance, it might make sense for the metadata topic to be a compacted topic, which would allow downstream processors to treat it as a changelog topic and then join it with the execution topic. However, this introduces a lot of complexity. If we go with a compacted topic, the the tricky part becomes, "what is the key?"

We would need to isolate between the different Getable types. We could use the `GetableClassEnum` as the prefix separator; however, that is leaking an internal implementation detail. Or we could create a completely new storage mechanism. We think it's best for our users to decide how to store it. Maybe htey only want to store certain TaskDef's; it would be best for them to read a normal (non-compacted) topic, filter it, and create their own materialized vieq

Note that most metadata in LittleHorse is immutable—when you want to change it, you end up creating a new version, which is a separate LittleHorse API Object with its own ID—so historical version mismatching shouldn't be a problem if the consumer is up-to-date on metadata but way behind on execution data.

### Multi-Tenancy

There are a few considerations regarding topic structure, ownership, and multi-tenancy:

* LittleHorse is multi-tenant: a certain `Principal` might be able to do something in `Tenant` `A` but not in `B`.
* Kafka topics and partitions are not "free." There is a metadata overhead to each topic in Kafka.
* In Kafka, it is not possible to give a client permission to read certain messages in a topic but not others. However, you can allow a certain client to read one topic but not another.

Due to the above reasons, I propose that:
1. Each LittleHorse `Tenant` gets its own Output Topics (one for `metadata` and `execution` data).
2. We utilize protobuf `oneof`s to allow putting all data into the two topics above, and clients can filter it out as needed.

This prevents an expensive proliferation of Kafka topics and partitions as much as possible while still allowing different LittleHorse `Tenant`s to have isolated data.

### Topic Naming

Since there will be one `metadata-output` topic for each `Tenant` and one `execution-output` topic for each `Tenant`, the topics will be named as follows:

* `f"{LHS_CLUSTER_ID}_{tenant}_execution-output"`
* `f"{LHS_CLUSTER_ID}_{tenant}_metadata-output"`

The `LHS_CLUSTER_ID` is the prefix of all topics for the cluster, so the LH Server already has permissions to write to it. By further isolating with `tenant_id` as the next step in the prefix, we can use Kafka's prefix-based ACL's to give a consumer permissions to read those specific topics.

### Partitioning

For the initial implementation, the `execution-output` topics will have a number of partitions determined by `LHS_OUTPUT_TOPIC_PARTITIONS`. We recommend making this a divisor of `LHS_CLUSTER_PARTITIONS` and using Kafka's rebalance mechanisms to ensure that the leader for the Command topic partition, the core-store-changelog partition, and the output topic partition all reside on the same broker. The reason for this is in order to allow batching all of those requests to Kafka.

In the future, we can make this configurable on a per-`Tenant` basis.

## Proto Schemas

LittleHorse is a protobuf-first system. The output topic will inherit this characteristic.

### Tenant

Users should be able to enable or disable the Output Topic on a per-tenant basis. This logically means that we should extend the `Tenant` message to include Output Topic configurations.

```protobuf
// Configurations for the Output Topic of a certain Tenant.
message OutputTopicConfig {
    // Enum to configure default recording level of Output Topic events.
    enum OutputTopicRecordingLevel {
        // Records all updates for entities from all `WfSpec`s, `TaskDef`s,
        // `WorkflowEventDef`s, `UserTaskDef`s, and `ExternalEventDef`s to 
        // the Output Topic by default.
        ALL_ENTITY_EVENTS = 0;

        // With this configuration, no events are sent to the Output Topic unless
        // explicitly enabled in the metadata object itself (to do with future work).
        NO_ENTITY_EVENTS = 1;
    }

    OutputTopicRecordingLevel default_recording_level = 1;
}

message Tenant {
    // ...

    // Configurations related to the Output Topic. If not set, then the Output
    // Topic is not enabled for the corresponding Tenant.
    optional OutputTopicConfig output_topic = 3;
}

message PutTenantRequest {
    // ...

    // Configures the behavior of the Output Topic for this Tenant. If not set,
    // then the OutputTopic is not considered to be enabled.
    optional OutputTopicConfig output_topic_config = 2;
}
```

### Output Topic Schemas

Every message in the `execution-output` topic will be an `OutputTopicRecord`, and we will make heavy use of `oneof` to allow multiple data types.

The initial implementation will allow six types of records to be pushed into the Output Topic. Each record will correspond to one of the Getable's below, and each record will (at first) simply contain a snapshot of the Getable itself:

1. **`TaskRun`**: Notifications when a taskrun is executed (failed, completed).
2. **`WorkflowEvent`**: Any `WorkflowEvent`s thrown by a `WfRun`.
3. **`WfRun`**: Notifications about changes to the status of a `WfRun` (at first, only the `WfRun` status, not status of specific `ThreadRun`s).
4. **`UserTaskRun`**: Updates for a `UserTaskRun`, every time a new entry is added to the `events` field.
5. **`ExternalEvent`**: a record every time an `ExternalEvent` is registered.
6. **`Variable`**: a record every time the value of a `Variable` is changed.

Of course, future Proposals may add additional records by extending the `oneof` structure.

```protobuf
// An OutputTopicRecord is a single record in the output topic, which can
// denote one of several different types of events.
message OutputTopicRecord {
    // The time at which the event occurred.
    google.protobuf.Timestamp timestamp = 1;

    oneof payload {
        // Records the results of a TaskRun in the Output Topic.
        TaskRun task_run = 2;

        // Records a WorkflowEvent that was thrown into the Output Topic.
        WorkflowEvent workflow_event = 3;

        // Records an update to a WfRun, triggered by a change to the status of a
        // `ThreadRun`.
        WfRun wf_run = 4;

        // Updates about a user task run.
        UserTaskRun user_task_run = 5;

        // Updates about a specific Variable changing.
        Variable variable = 6;

        // Updates about an `ExternalEvent` changing.
        ExternalEvent external_event = 7;
    }
}
```

The Output Topic itself can be set to `cleanup.policy=delete` with any retention time and/or Tiered Storage (KIP-405) settings that the Kafka cluster admin wants.

### Metadata Output Topic

The Metadata Output Topic is intended for consumers of the output topic to have a global view of the metadata in the `Tenant` so that they can perform joins. As of now, there's no reason why it can't be a compacted, changelog-style topic with an entry for every live piece of metadata. 

The topic can look something like this:

```protobuf
// Message to configure data sent to the Metadata Output Topic
message MetadataOutputTopicRecord {
    // The data that was sent
    oneof metadata_record {
        // A WfSpec update
        WfSpec wf_spec = 1;

        // A TaskDef update
        TaskDef task_def = 2;

        // An ExternalEventDef Update
        ExternalEventDef external_event_def = 3;

        // A WorkflowEventDef update
        WorkflowEventDef workflow_event_def = 4;

        // A UserTaskDef update
        UserTaskDef user_task_def = 5;
    }
}
```

There is no need to register `Principal`s, since they're not scoped to the tenant, and there's not much useful business info in the creation of `Principal`s. Obviously, since the Output Topic is tenant scoped, we don't put `Tenant`s there.

## Configuring What's Sent

This initial Proposal has the following philosophy:

* Minimal configuration options needed to get the feature to work.
* Design an API that can be extended in future Proposals to allow for fine-grained control of what is sent to the Output Topic.

The TLDR is that the `Tenant` configurations (see above) determine whether _by default_ all Getables are enabled to the Output Topic or if no Getables are enabled to the Output Topic. This behavior can be overriden at the _metadata_ level for each `Getable` (eg. enabled for a specific `TaskDef` when the `Tenant` has `NO_ENTITY_EVENTS` set, or disabled for a specific `WfSpec` when the `Tenant` has `ALL_ENTITY_EVENTS` set).

### Metadata Getable Changes

We will add a boolean field as follows to the `WfSpec`, `TaskDef`, `WorkflowEventDef`, `UserTaskDef`, `ExternalEventDef`, and `ThreadVarDef` messages, along with the corresponding `Put{}Request` messages:

```proto
  // Configures whether entity events are sent for the `Getable`s associated with
  // this metadata object. If not set, then the default behavior for the `Tenant` is
  // used.
  optional bool enable_entity_events = ?;
```

Versioned Metadata Getables should _not_ create a new version when we update the `enable_entity_events` flag. It should update in-place rather than creating a new metadata `Getable`.

For a `Tenant` with `ALL_ENTITY_UPDATES` as the configured output topic setting, then any `PUBLIC_VAR` `Variable` will be journalled to the Output Topic. Other variables will _not_ be journaled. However, users may override this behavior on a case-by-case basis by doing something like this:

```java
WfRunVariable myVar = wf.declareStr("some-var").asPublic().withoutOutputTopic();
```

## Implementation

We will add another sink to the Core Processor (in `ServerTopology.java`) which determines the topic dynamically based on the `Tenant` information.

We will place a "sniffer" in the `CoreProcessorContext` that watches for `put()`'s with updates to `Getable`s, and then in the `endExecution()` method does a series of `context.forward()`'s for any `Getable`s that changed and are configured to be sent to the Output Topic.

### Reads vs Puts

Currently, we have a weakness in the `GetableUpdates` inside the `CoreProcessorContext`. Every time we _read_ a `Getable`, we put it in the buffer of changes. The reason for this is:

* To cache reads, meaning that we only read a single `Getable` from RocksDB once per `Command`.
* To prevent multiple `Model` copies of the same `Getable` from floating around in the code during one single `Command` processing.
* To make it so that if you read a `Getable` and modify the `Model`, the changes are reflected in RocksDB.

We do not check whether there were actually any modifications before we do a `rocksdb.put()`. This is fine—it's only a very small performance hit and has no impact on correctness.

However, if we utilize the `GetableUpdates` in its current form for the Output Topic, this would mean that a `.get()` would _incorrectly_ result in a new record in the Output Topic. To fix this, we need to compare the new value of the `Getable` with the old value, and only forward _changed_ `Getable`s to the Output Topic. During this implementation we can also improve performance a little bit by not doing an extra write to RocksDB.

### Testing

Automated end-to-end testing for the Output Topic will be hard. We can manually write an end-to-end test ("manual" because we can't simply use the `test-utils` framework) that runs some basic `WfRun`'s, starts a Kafka Consumer, and asserts that the correct records are sent to the Output Topic.

However, most of the complicated parts should be tested in unit tests rather than in e2e tests, as we will need to do fine-grained analysis of all of the features (especially the overrides that configure which `Getable`s are sent to the Output Topic).

## Future Work

Future work may include:

* Directly configuring what variables are sent rather than relying only upon the `access_level` of the `ThreadVarDef`.
* Improved External Event visibility.
  * For example, a user might want to have enough information to be able to deduce how long on average we are waiting for an `ExternalEventNode`. This would require information about the `ExternalEventNodeRun` itself rather than just the `ExternalEvent`.
  * This can be solved with a solution specific to `ExternalEvent`s or it can be solved by allowing `NodeRun`-level events. It will be left to a future Proposal pending user feedback.

The above is out-of-scope for this proposal. Also note that this proposal can be implemented in phases:

* Adding the Metadata Output Topic.
* Adding the event Output Topic without fine-grained configuration of what events are sent.
  * We can even enable events for different `Getable`s in different PR's.
* Allowing fine-grained configuration