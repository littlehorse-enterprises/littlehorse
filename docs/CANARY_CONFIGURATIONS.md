# LH Canary Configurations

## What is LH Canary?

A Canary is a species of bird that was used in the past by miners to detect toxic fumes.

The LH Canary is a **Synthetic Monitoring Application** that emulates the paths users might take when
using a LH Cluster. It is useful to detect bugs, failures, and outages.

LH Canary take inspiration on **Strimzi Canary**. Strimzi Canary is a part of the Strimzi project that sits outside your Kafka cluster
and simulates activity to identify problems from a user perspective. It can be used to raise alerts when a Kafka cluster
is not operating as expected. The Canary tool creates a dedicated “Canary” topic with partitions on every broker, sends
messages to the Canary Topic. Each message has a payload containing an ID and a timestamp. The Canary tool also consumes
the messages and measures the latency (via the timestamp), and exposes such metrics in the Prometheus format.

The LH Canary is highly similar to the Strimzi Canary, but it is slightly more complex. Whereas the Strimzi Canary just
needs to produce messages to a topic and make sure they go through, the LH Canary emulates an E2E use case where a
client requests to run a workflow and then a worker executes the schedules task.

As you can see in the following image, LH Canary has three components an `Aggregator`, a `Metronome` and
a `LH TaskWorker`.

### Metronome

It's an application that executes a [RunWf](https://github.com/littlehorse-enterprises/littlehorse/blob/master/schemas/service.proto#L75)
in a constant interval (ex: every 500ms).

T

### Task Worker

### Aggregator

![](../docs/images/lh-canary.png)

## Kafka Configurations

