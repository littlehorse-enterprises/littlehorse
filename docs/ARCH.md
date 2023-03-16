# LittleHorse Arch
LittleHorse is a Microservice Orchestration Engine. For a description of the problem statement, see the LittleHorse Overview. It’s also recommended to read the `PROGRAMMING_MODEL.md` in the io-littlehorse-jlib repo to get a background for how Workflow logic works.

The main component of the LittleHorse system is the LH Server. All metadata is managed through the LH Server, as are all workflow runs.

# LH Server
The LH Server comprises several logical parts:

* A gRPC server with a public API that clients communicate with.

* A central “Command” Kafka Topic.

* A Kafka Streams topology which reads the Command topic, and performs all logic.

The gRPC server’s API is quite simple and can be seen in `proto/service.proto` from the io-littlehorse-jlib repository.

However, the implementation is quite complex. All state for the server is persisted in Kafka Streams topologies. Before reading further, I recommend gaining a primer on how Kafka Streams works. For example, the following links are good:

* [Short Overview Blog Post](https://lucapette.me/writing/getting-started-with-kafka-streams/?utm_source=atom_feed) (Recommended)

* [Long Read from Hevo Data](https://hevodata.com/learn/kafka-streams/)

* [Kafka Streams Developer Guide](https://kafka.apache.org/33/documentation/streams/developer-guide/)

## Core Streams Topology
The bulk of the LittleHorse system is built upon an event-sourcing architecture. Basically:
Events are recorded to the Core Command Kafka Topic.
Those events are processed in order** and the state of the system is updated accordingly.

** in order on each partition. No ordering guarantees are made across partitions.

Such events are called “Commands” and are defined in `proto/command.proto`. As you would expect, all messages in the Core Command topic are just serialized Command protobuf’s.

To give a more concrete idea of what the Commands do, they generally record:

* Request to create/delete metadata such as WfSpec, TaskDef, ExternalEventDef

* Request to run a WfRun

* Registration of an ExternalEvent

* Reporting the result of a TaskRun (completed, error, timed out, etc)

* Request by a Task Worker to claim a scheduled Task (discussed in more detail later).

## Consistent, Synchronous POST Responses (Metadata)
Kafka is generally considered an “asynchronous” system. However, request-response API's in LittleHorse need to provide fast, synchronous, and strongly consistent responses. That is slightly tricky, but it is implemented in the `KafkaStreamsServerImpl` class.

At a high level:

1. The gRPC request handlers write commands to the Core Command Topic, and wait by enqueueing the gRPC response `StreamObserver` handle into a queue.
2. The Core Streams topology processes the Commands in order
3. For each command, the CommandProcessor:
    1. Processes the command and updates the state of the system (advancing a workflow run, creating a WfSpec, etc) in the RocksDB store on disk.
    2. Returns a response to the waiting gRPC response `StreamObserver` handle from the queue in the first step.

For more info on what a `StreamObserver` is, look up a tutorial for server-side gRPC in Java (both unary and streaming).

How does this work in the code? 

1. The [KafkaStreamsServerImpl](../app/src/main/java/io/littlehorse/server/KafkaStreamsServerImpl.java) class has a `processCommand()` method, which takes in a gRPC request (raw protobuf), the response `StreamObserver` handle, and the `Class` for the command and response (used for serializing and deserializing with reflection).
2. The `processCommand()` method creates a [POSTStreamObserver](../app/src/main/java/io/littlehorse/server/streamsimpl/util/POSTStreamObserver.java) which wraps the response `StreamObserver` handle.
3. Next, the `processCommand()` method records the `command` to the Command Kafka Topic.
    1. In the Kafka Producer's `callback`, if there is an error, the `POSTStreamObserver` is notified that the request failed. If the request succeeds, the method `BackendInternalComms::waitForCommand()` is called with the `POSTStreamObserver`.
4. Once the `Command` message from the previous step is processed by the Kafka Streams topology (see [CommandProcessor.java](../app/src/main/java/io/littlehorse/server/streamsimpl/coreprocessors/CommandProcessor.java)), the result of the processing is sent to the `POSTStreamObserver` from step 1.

## Consistent, Synchronous Task Dispatching
There is a highly similar mechanism for the dispatching of tasks. While the concepts are the same, the internal implementation uses different infrastructure which is discussed in this section.

In the above section, we discussed the synchronous request-response flow, in which all responses had one direct cause (the request from the client). However, in this section, there are a few distinctions:
* Clients use gRPC long-polling (aka biderectional streaming) rather than unary calls.

* Two conditions must be met before the response can be returned:

  * A `TaskRun` has been scheduled by the CommandProcessor.

  * One of the clients waiting to execute `TaskRun`'s of that type is ready to accept more work.

The way this protocol works is:

1. The `LHTaskWorker` client initiates the `registerTaskWorker()` unary gRPC call, which returns a list of Server URL's that the `LHTaskWorker` should connect to.
2. The `LHTaskWorker` opens a `pollTask()` streaming request with each of the Server URL's returned from step 1.
4. On each `pollTask()` connection, the `LHTaskWorker` sends a `PollTaskPb` message.
5. The server handles the `pollTask()` stream by creating a [PollTaskRequestObserver](../app/src/main/java/io/littlehorse/server/streamsimpl/taskqueue/PollTaskRequestObserver.java).
6. Upon receipt of the `PollTaskPb` request from step 4, the `PollTaskRequestObserver` from step 5 enqueues itself onto an internal "hungry clients" queue for the specified TaskDef.
    1. This is handled using the [TaskQueManager](../app/src/main/java/io/littlehorse/server/streamsimpl/taskqueue/TaskQueueManager.java) class, which has one instance of the [OneTaskQueue](../app/src/main/java/io/littlehorse/server/streamsimpl/taskqueue/OneTaskQueue.java) clss for each `TaskDef`.
7. When the [CommandProcessorDaoImpl](../app/src/main/java/io/littlehorse/server/streamsimpl/coreprocessors/KafkaStreamsLHDAOImpl.java) schedules a task via `scheduleTask()`, the `KafkaStreamsServerImpl::onTaskScheduled()` method is called.
    1. This calls the `onTaskScheduled()` method of the `TaskQueueManager` class.
    2. Essentially, this enqueues the Task onto the internal task queue, just as clients were enqueued onto the internal "hungry clients" queue in step 6.
8. When there is a task in the internal task queue, and a `PollTaskRequestObserver` in the hungry clients queue, the task is matched and returned to the client.

### Returning Tasks to the Client
This is a brief description of the `KafkaStreamsServerImpl::returnTaskToClient()` method.

At step 8 above, we're pretty confident that the `TaskRun` hasn't yet been dispatched to a client. However, as you will soon see, we're not 100% sure.

First, we must send the [TaskClaimEvent](../app/src/main/java/io/littlehorse/common/model/command/subcommand/TaskClaimEvent.java) command. When the `TaskClaimEvent` is processed, it will succeed if the `TaskRun` hasn't yet been scheduled, and fail if it has already been scheduled.

ONLY if the `TaskRun` hasn't been scheduled do we pop the `PollTaskRequestObserver` from the hungry client queue and dispatch the task back to it. If we were to jump the gun and immediately return the task, it's possible that the same task gets dispatched twice (in the case of a Server crash between the time the task was first dispatched and the time that the `TaskClaimEvent` was processed by the `CommandProcessor`).

## Retrieving Data: Kafka Streams Interactive Queries
All data in LittleHorse is stored in the Kafka Streams Topology. It is accessed through [Interactive Queries](https://docs.confluent.io/platform/current/streams/developer-guide/interactive-queries.html).

Readers are referred to the above link for a general overview of how IQ works. In the interest of brevity, this section mainly covers the LH-Specific Sections.

### Core Metadata Processing
Core Metadata such as `WfSpec`, `TaskDef`, and `ExternalEventDef` has some unique properties:
* There is a relatively small amount of this data.
* There are few changes to it.
* It is needed for `WfRun` processing on all partitions.

Therefore, the data can and needs to be available in a Kafka Streams `GlobalStore` (consult Kafka Streams documentation for an idea of what that is).

However, the processing for these metadata items needs to happen in an actual processor for various reasons, chief among them that the input to a `GlobalStore` needs to just be a raw changelog of puts/deletes, and any processing logic passed into the Streams Global State Store Builder is just ignored as per this [Kafka Jira](https://issues.apache.org/jira/browse/KAFKA-7663).

Therefore, what we do is assign the same partition key for *all* global metadata objects, so that it all ends up on the same processor. Then, the resulting global metadata events are also forwarded to the `global-metadata` topic, which is a changelog for the `GlobalStore`.

### Secondary Index: "Tagging"
The Interactive Queries doc referenced above does not contain good information about how to perform lookups on secondary indexes. 

More info will be added here later.
<!-- TODO -->

# Explanations
There are a few design decisions that at first glance warrant some explanation.

## Why not a Normal Database?
The Kafka Streams-based architecture provides great scalability, low latency, and durability without sacrificing strong consistency. Such performance could not be achieved using a Postgres backend.

Streams is horizontally scalable.
Colocating the processing and the data on the same node allows for very fast lookups of WfRun data.
Ordering events within a partition allows for strong consistency without global locking that leads to a bottleneck when using traditional databases such as Postgres.

Such scalability could be achieved using Cassandra (Temporal does this), however, Temporal needs about 3-5x as much hardware to achieve the same throughput, and latencies are much higher.

Conductor uses multiple backend systems (dynamo + redis queues + elasticsearch) together, and is nearly (but not quite) as performant. However, their consistency guarantees are unclear as they have data moving through multiple systems. The behavior of the system during a crash (eg. are tasks duplicated or dropped) is not well-documented. In LH, we have exactly-once guarantees.

Zeebe has a highly similar architecture and similar scalability; however, they basically implemented Kafka from scratch on their own. Which is harder than what we did, and we still have similar performance, better observability, and better integrations with external systems.

## Why not Pulsar?
Pulsar is superior to Kafka in terms of multi-tenancy, elasticity (support for tiered storage), and multi-region replication. However, there are three issues with Pulsar:

### Operational Difficulties.
* There are few vendor options for Pulsar, and all are expensive.
* The OSS K8s operators for Pulsar are all inferior to the Strimzi Kafka operator. All Pulsar operators are just helm charts, which are considered at best “Level 2” on operatorhub.io (meaning they only do basic install + basic upgrades).
* In contrast, Strimzi is “Level 4”, and its controller supports advanced app lifecycle management, storage management, metadata management (Kafka Topics and Kafka Users), certificate rotation, Prometheus integration, and complex scaling operations.
* Pulsar requires two zookeeper clusters, bookkeeper, and Pulsar brokers.
* Kafka requires one zookeeper cluster and Kafka. Soon, it will only require Kafka, which removes the need to manage three distributed systems (two ZooKeeper and one BookKeeper cluster).

### No sufficient stream processing solution.
* Flink does not provide Interactive Queries (ability to query state dynamically).
* Flink’s support for exactly-once semantics involves 30-second commits, which is too slow for LittleHorse.
* Flink also doesn’t provide any way to implement the scheduler queues that LH has.

### Less mature technology
* Pulsar is used by fewer organizations, has poorer documentation and community, and also more bugs.

## Why Java?
As discussed above, Kafka Streams was the best fit. And it’s a Java library, which means we had to use Java.

Alternatively, we could have built a simple version of Kafka Streams in GoLang, but Streams itself is 65k lines of code (the section that is relevant to LH is over 35k lines) and involves some very complex distributed systems logic. Too much work and too risky to replicate.

Building in GoLang would have also enabled us to use Pulsar; however, Pulsar's implementation of partitioning (key-shared subscriptions) doesn't lend itself well to building distributed databases. Kafka's partition assignment mechanism in which each topic has a pre-configured number of partitions, and a rebalance protocol propagates info about which consumer owns which partitions, is highly suited to building distributed databases. Such is exactly how Yugabyte works under the hood.

## Why One Big State Store?
Experienced Kafka Streams users would note that it is odd that LH opts to use one gigantic `Bytes` state store rather than having multiple different state store (eg one for `WfRun`, one for `NodeRun`, one for `Variable`, one for `Tag`, etc). This is purely a performance optimization.

An earlier version of LittleHorse was implemented with multiple state stores as is customary (I believe there were 19 stores, total). The internal Streams consumer rebalances took about 20-30 seconds with only a few Streams instances.

Once the "one-big-store" architecture was adopted, the Streams consumer rebalance time dropped to about 3 seconds. This is a massive improvement to availability.