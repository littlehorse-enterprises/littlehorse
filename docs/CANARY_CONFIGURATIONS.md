# LH Canary Configurations

<!-- TOC -->
* [LH Canary Configurations](#lh-canary-configurations)
  * [What is LH Canary?](#what-is-lh-canary)
  * [Metronome](#metronome)
    * [Kafka Topics](#kafka-topics)
    * [Configurations](#configurations)
      * [`lh.canary.metronome.enable`](#lhcanarymetronomeenable)
      * [`lh.canary.metronome.frequency.ms`](#lhcanarymetronomefrequencyms)
      * [`lh.canary.metronome.threads`](#lhcanarymetronomethreads)
      * [`lh.canary.metronome.runs`](#lhcanarymetronomeruns)
    * [Kafka Configurations](#kafka-configurations)
    * [LH Client Configurations](#lh-client-configurations)
  * [Task Worker](#task-worker)
    * [Kafka Topics](#kafka-topics-1)
    * [Configurations](#configurations-1)
      * [`lh.canary.metronome.worker.enable`](#lhcanarymetronomeworkerenable)
    * [Kafka Configurations](#kafka-configurations-1)
    * [LH Client Configurations](#lh-client-configurations-1)
  * [Aggregator](#aggregator)
    * [Metrics](#metrics)
    * [Kafka Topics](#kafka-topics-2)
    * [Configurations](#configurations-2)
      * [`lh.canary.aggregator.enable`](#lhcanaryaggregatorenable)
      * [`lh.canary.aggregator.store.retention.ms`](#lhcanaryaggregatorstoreretentionms)
      * [`lh.canary.metrics.port`](#lhcanarymetricsport)
      * [`lh.canary.metrics.path`](#lhcanarymetricspath)
      * [`lh.canary.metrics.filter.enable`](#lhcanarymetricsfilterenable)
      * [`lh.canary.metrics.filter.enable.<metric name>`](#lhcanarymetricsfilterenablemetric-name)
      * [`lh.canary.metrics.common.tags.<additional tag>`](#lhcanarymetricscommontagsadditional-tag)
    * [Kafka Configurations](#kafka-configurations-2)
  * [Common Configurations](#common-configurations)
      * [`lh.canary.id`](#lhcanaryid)
      * [`lh.canary.topic.name`](#lhcanarytopicname)
      * [`lh.canary.topic.creation.enable`](#lhcanarytopiccreationenable)
      * [`lh.canary.topic.creation.replicas`](#lhcanarytopiccreationreplicas)
      * [`lh.canary.topic.creation.partitions`](#lhcanarytopiccreationpartitions)
      * [`lh.canary.topic.creation.timeout.ms`](#lhcanarytopiccreationtimeoutms)
  * [Using Env Variables](#using-env-variables)
<!-- TOC -->

## What is LH Canary?

A Canary is a species of bird that was used in the past by miners to detect toxic fumes.

The LH Canary is a **Synthetic Monitoring Application** that emulates the paths users might take when
using a LH Cluster. It is useful to detect bugs, failures, and outages.

LH Canary take inspiration on **Strimzi Canary**. Strimzi Canary is a part of the Strimzi project that sits outside your
Kafka cluster
and simulates activity to identify problems from a user perspective. It can be used to raise alerts when a Kafka cluster
is not operating as expected. The Canary tool creates a dedicated “Canary” topic with partitions on every broker, sends
messages to the Canary Topic. Each message has a payload containing an ID and a timestamp. The Canary tool also consumes
the messages and measures the latency (via the timestamp), and exposes such metrics in the Prometheus format.

The LH Canary is highly similar to the Strimzi Canary, but it is slightly more complex. Whereas the Strimzi Canary just
needs to produce messages to a topic and make sure they go through, the LH Canary emulates an E2E use case where a
client requests to run a workflow and then a worker executes the scheduled task.

As you can see in the following image, LH Canary has three components an `Aggregator`, a `Metronome` and
a `LH TaskWorker`.

![](../docs/images/lh-canary.png)

## Metronome

It's an application that executes
a [RunWf](https://github.com/littlehorse-enterprises/littlehorse/blob/master/schemas/service.proto#L75)
in a constant interval (ex: every 500ms).
It is configurable to simulate different scenarios.

### Kafka Topics

- `canary-beats` use for producing the metric beats.

### Configurations

#### `lh.canary.metronome.enable`

Flag to enable the metronome component.

- **Type:** boolean
- **Default:** true
- **Importance:** high

---

#### `lh.canary.metronome.frequency.ms`

Time between requests.

- **Type:** int
- **Default:** 1000
- **Importance:** medium

#### `lh.canary.metronome.threads`

Size of thread pool.

- **Type:** int
- **Default:** 1
- **Importance:** medium

---

#### `lh.canary.metronome.runs`

Number of wf run to request.

- **Type:** int
- **Default:** 1
- **Importance:** medium

### Kafka Configurations

LH Canary supports all kafka configurations. Use the prefix `lh.canary.` and append the kafka config.
Examples

- For `security.protocol`, use `lh.canary.security.protocol`.
- For `bootstrap.servers`, use `lh.canary.bootstrap.servers`.

### LH Client Configurations

LH Canary supports all LH Client configurations. Use the prefix `lh.canary.` and append the LH config.
Change `_` to `.`.

Examples:

- For `LHC_API_HOST`, use `lh.canary.lhc.api.host`. In case of using env variables: `LH_CANARY_LHC_API_HOST`.
- For `LHC_API_PORT`, use `lh.canary.lhc.api.port`. In case of using env variables: `LH_CANARY_LHC_API_PORT`.

## Task Worker

Received and execute tasks previously schedule by the metronome.
This allows worker to calculate the latency since the workflow run was scheduled
to when it was executed by the worker.

### Kafka Topics

- `canary-beats` use for producing the metric beats.

### Configurations

#### `lh.canary.metronome.worker.enable`

Flag to enable the worker component.

- **Type:** boolean
- **Default:** true
- **Importance:** high

### Kafka Configurations

LH Canary supports all kafka configurations. Use the prefix `lh.canary.` and append the kafka config.
Examples

- For `security.protocol`, use `lh.canary.security.protocol`.
- For `bootstrap.servers`, use `lh.canary.bootstrap.servers`.

### LH Client Configurations

LH Canary supports all LH Client configurations. Use the prefix `lh.canary.` and append the LH config.
Change `_` to `.`.

Examples:

- For `LHC_API_HOST`, use `lh.canary.lhc.api.host`. In case of using env variables: `LH_CANARY_LHC_API_HOST`.
- For `LHC_API_PORT`, use `lh.canary.lhc.api.port`. In case of using env variables: `LH_CANARY_LHC_API_PORT`.

## Aggregator

It's a kafka streams application that consumes the metric beats and
aggregates them into average and max metrics.

It exposes a `/metrics`  endpoint that prometheus scrapes.

### Metrics

| Metric                          | Description                                                                                                  |
|---------------------------------|--------------------------------------------------------------------------------------------------------------|
| `task_run_latency_avg`          | Average time elapsed from when a task was scheduled until it was executed by the worker in milliseconds      |
| `task_run_latency_max`          | Maximum time to execute a task by the worker in milliseconds                                                 |
| `run_wf_latency_avg`            | Average time of requesting a new wf run in milliseconds                                                      |
| `run_wf_latency_max`            | Max time of requesting a new wr run in milliseconds                                                          |
| `duplicated_task_run_max_count` | Number of detected duplicated task. Useful for data integrity, every task scheduled has to have an unique id |

### Kafka Topics

- `canary-beats` use for producing the metric beats.
- Kafka streams topics:
    - `canary-dev-duplicated-task-by-server-count-changelog`
    - `canary-dev-duplicated-task-by-server-count-repartition`
    - `canary-dev-duplicated-task-count-changelog`
    - `canary-dev-latency-avg-changelog`
    - `canary-dev-metrics-changelog`
    - `canary-dev-metrics-repartition`

### Configurations

#### `lh.canary.aggregator.enable`

Flag to enable the aggregator component.

- **Type:** boolean
- **Default:** true
- **Importance:** high

---

#### `lh.canary.aggregator.store.retention.ms`

Total kafka streams store retention in milliseconds.

- **Type:** long
- **Default:** 7200000
- **Importance:** medium

---

#### `lh.canary.metrics.port`

Prometheus scrape endpoint port.

- **Type:** int
- **Default:** 4023
- **Importance:** medium

---

#### `lh.canary.metrics.path`

Prometheus scrape endpoint path.

- **Type:** string
- **Default:** /metrics
- **Importance:** medium

---

#### `lh.canary.metrics.filter.enable`

Flag to enable the metric filter.
If `false` all metrics are exposed without filter.
If `true` the filter is enabled and only the metrics under the `lh.canary.metrics.filter.enable.<metric name>` config
will be exposed.

By default, it is enabled, and the aggregator only exposed the canary related metrics.

- **Type:** boolean
- **Default:** true
- **Importance:** high

---

#### `lh.canary.metrics.filter.enable.<metric name>`

This config is useful to enable metrics outside the canary related metrics.
For example, if you want to activate `kafka_stream_alive_stream_threads` metric, you have to pass:
`lh.canary.metrics.filter.enable.kafka_stream_alive_stream_threads=true`.

- **Type:** boolean
- **Default:** null
- **Importance:** low

---

#### `lh.canary.metrics.common.tags.<additional tag>`

This config is useful to add default tags to prometheus metrics.
For example: `lh.canary.metrics.common.tags.my_tag=my-value`.

- **Type:** string
- **Default:** null
- **Importance:** low

### Kafka Configurations

LH Canary supports all kafka configurations. Use the prefix `lh.canary.` and append the kafka config.
Examples

- For `state.dir`, use `lh.canary.state.dir`
- For `bootstrap.servers`, use `lh.canary.bootstrap.servers`

## Common Configurations

#### `lh.canary.id`

An identifier for LH Canary. Useful for the internal components like: kafka streams, kafka producers and LH workers.

- **Type:** string
- **Default:** canary-default
- **Importance:** high

---

#### `lh.canary.topic.name`

Metrics beats topic. Use by the aggregator for consuming, and for the metronome and worker for producing metric beats.

- **Type:** string
- **Default:** canary-beats
- **Importance:** high

---

#### `lh.canary.topic.creation.enable`

Flag to enable topics creation.

- **Type:** boolean
- **Default:** false
- **Importance:** high

---

#### `lh.canary.topic.creation.replicas`

Replicas for metric beats topic.

- **Type:** int
- **Default:** 3
- **Importance:** high

---

#### `lh.canary.topic.creation.partitions`

Partitions for metric beats topic.

- **Type:** int
- **Default:** 12
- **Importance:** high

---

#### `lh.canary.topic.creation.timeout.ms`

Total time to wait for creating the metric beats topic in milliseconds.

- **Type:** int
- **Default:** 5000
- **Importance:** high

## Using Env Variables

LH Canary has support for env variables.
Properties defined via env variables will override the value of that property defined in file input and default config.

To construct the environment key variable name for server.properties configs, following steps can be followed:-

* Replace `.` with `_`.
* Examples:
    * For `lh.canary.id`, use `LH_CANARY_ID`.
    * For `lh.canary.bootstrap.servers`, use `LH_CANARY_BOOTSTRAP_SERVERS`.

By default, de log level for the canary is `INFO`. It is possible to change this
setting the env `LOG_LEVEL`.
