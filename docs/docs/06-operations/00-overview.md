# Operations Overview

A minimal LittleHorse cluster has the following components:

* LH Server (eg. the [`lh-server`](https://gallery.ecr.aws/littlehorse/lh-server) docker image)
* A Kafka Cluster

With just this setup, you can run a working LittleHorse cluster, potentially relying on mTLS authentication for security. If you wish to delegate authentication to an OAuth Server, we introduce an additional architectural component:

* OAuth Identity Provider

The IdP can be used to authenticate machine clients as well as human clients (eg. a human using `lhctl` or the LH Dashboard). Speaking of the Dashboard, users who wish to run LittleHorse can also introduce:

* The LH Dashboard, usually via the [`lh-dashboard`](https://gallery.ecr.aws/littlehorse/lh-dashboard) dockerimage

Lastly, a production-ready deployment of any technology requires monitoring. The LittleHorse Server exposes prometheus-compatible metrics (by default, on the port `1822` in honor of the year of Ecuador's independence). As such, you might introduce one final component:

* A prometheus-compatible monitoring system.

## LH Server

The LH Server internally is a stateful Kafka Streams application, and it exposes a public [GRPC API](/docs/developer-guide/grpc). Note that Kafka is an implementation detail of LittleHorse and, as of `0.7.0`, is not exposed to the LittleHorse clients.

At its core, the LH Server is itself just a Java application that stores information locally on disk and talks to a Kafka cluster. Therefore, all you _really_ need is a disk, a Kafka Cluster, and a JVM.

### Persistent Storage

The LittleHorse Server processes all data and serves all queries from RocksDB. RocksDB stores data in SST files on disk. The LittleHorse Server uses disk to persist the data stored on RocksDB between server restarts (i.e. during a rolling upgrade or after a crash recovery). If an LH Server instance starts up and data is missing, then the data on RocksDB is re-constructed by replaying Kafka changelog topics. This process is time-consuming but it does ensure reliability so long as your Kafka cluster is durable. However, this process can largely be avoided by providing persistent storage to the LH Server.

The most important takeaway from this section is that **the LH Server is stateful**, so you should provision sufficient storage to handle your workloads, and also ensure that you monitor free disk space.

### Advertised Listeners

For most uses of the [GRPC API](/docs/developer-guide/grpc), the client can connect to any LH Server in the cluster, and the contacted server will transparently route the request to the appropriate other LH Server Instance(s), and return the final result back to the client. For this use-case, the clients do not need the ability to connect to a specific LH Server Instance.

However, the Task Workers need to be able to address individual servers directly. This is because, to avoid costly distributed coordination, a scheduled `TaskRun` is only managed and maintained by a single LH Server, and the internal Task Queue's are partitioned by the server. Therefore, in order to ensure that all `TaskRun`'s are completed, the Task Workers for a certain `TaskDef` collectively need to connect to all of the LH Server Instances.

In order to reduce wasteful network connections, the LH Server has a Task Worker Assignment Protocol which, upon every Task Worker Heartbeat, assigns a list of LH Server Instances for each Task Worker to connect to.

As a consequence, every LH Server needs to have an "advertised" host and port for each configured internal listener, so that the Task Workers can "discover" where to connect to.

:::info
This all sounds really complicated, but don't worry! It happens transparently under the hood in our Task Worker SDK. You won't have to worry about balancing Task Workers; all you need to do is configure advertised listeners! This is similar to Kafka Consumer Groups.
:::

### Internal Listeners

LittleHorse is a partitioned system, meaning that not all data lives on all of the nodes. Therefore, when a request arrives on Server Instance 1, instance 1 may have to ask Instance 2 for the answer! LittleHorse has a special port for LH Server Instances to communicate with each other.

### Kafka Streams

LittleHorse is built on Kafka Streams because, quite simply, there was no other way to reach the performance numbers we wanted with any other backing data store (note: benchmarks are coming soon!).

It's safe to say that Kafka Streams is an incredibly powerful and beautiful piece of technology. However, with great power comes great complexity, so it's advisable that you understand Streams at a basic level before running the LittleHorse Server in production.

For some primers on Kafka Streams operations, our friends at [Responsive](https://responsive.dev) have posted some fantastic [Blog Posts](https://responsive.dev/blog) that you should check out. These blogs are general to Kafka Streams, not LittleHorse, but we have considered those topics when running LittleHorse in production for LittleHorse Cloud.

:::info
If you are concerned about the complexity of running LittleHorse in production, don't worry! You can also use our [Cloud Service](https://littlehorse.io/lh-cloud), or get expert support from LittleHorse Enterprises when running on-premise.
:::

## Kafka

Properly configuring Kafka is necessary for a production-ready LittleHorse installation.

### Topics

LittleHorse is internally a Kafka Streams application with [four sub-topologies](../02-architecture-and-guarantees.md#kafka-streams-topologies). These topologies require having proper Kafka Topics configured. The required topics are:

* Core Command Topic
  * `"{LHS_CLUSTER_ID}-core-cmd"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`
* Core Changelog Topic
  * `"{LHS_CLUSTER_ID}-core-store-changelog"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`
* Repartition Command Topic
  * `"{LHS_CLUSTER_ID}-core-repartition"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`
* Repartition Changelog Topic
  * `"{LHS_CLUSTER_ID}-core-repartition-store-changelog"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`
* Metadata Command Topic
  * `"{LHS_CLUSTER_ID}-global-metadata-cl"`
  * Partition Count: 1
* Metadata Changelog Topic
  * `"{LHS_CLUSTER_ID}-global-metadata-cl"`
  * Partition Count: 1
* Timer Command Topic
  * `"{LHS_CLUSTER_ID}-timers"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`
* Timer Changelog Topic
  * `"{LHS_CLUSTER_ID}-timer-changelog"`
  * Partition Count: `LHS_CLUSTER_PARTITIONS`

### Security

We recommend that you create a Kafka Principal for the LH Server. It requires the following permissions:

* Topics:
  * `DESCRIBE`
  * `DESCRIBECONFIGS`
  * `IDEMPOTENTWRITE`
  * `WRITE`
  * `READ`
  * `CLUSTERACTION`
* Groups:
  * `ALL`
* Transaction ID:
  * `ALL`

For security, all rules should be scoped to **only** entities with a prefix matching the `LHS_CLUSTER_ID`.


### Workload

It should be noted that the LittleHorse workload heavily uses Kafka Transactions and compacted topics. In particular, the transaction-heavy nature of the workload means that, relative to other Kafka workloads, the brokers used by LittleHorse will require a higher ratio of CPU to Network Bandwidth.

As with all Kafka deployments, it is strongly recommended to provision significant memory for your Kafka brokers so that tail-reading consumers (i.e. the LH Server) can read data fresh off the Kafka Broker's page cache rather than reading from disk. This has a significant effect on the latency of the LH Server.
