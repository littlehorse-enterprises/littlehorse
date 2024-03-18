import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Quickstart Installation

The minimum to get started with LittleHorse is to install the `lhctl` CLI, run the server in docker, and use your choice of the Java, GoLang, or Python client libraries.

:::tip
For fast responses, we recommend you join our [LittleHorse Community Slack Workspace](https://launchpass.com/littlehorsecommunity) to exchange ideas about and receive support for LittleHorse.
:::

Your system will need:
* `brew` or `go` to install `lhctl` (if you use `go`, make sure to put `~/go/bin` in your `PATH`).
* `docker` to run the LH Server.
* Either `go`, `java`, or `python` to run the client libraries.

## LittleHorse CLI

First, install `lhctl`, the LittleHorse CLI.

<Tabs>
  <TabItem value="brew" label="Homebrew" default>

Installation via Homebrew has been tested on Mac and Linux.

```
brew install littlehorse-enterprises/lh/lhctl
```
  </TabItem>
  <TabItem value="go" label="Go">

Alternatively, you can install `lhctl` directly from source using GoLang. Please remember to put `~/go/bin/` in your `PATH`.

```
go install github.com/littlehorse-enterprises/littlehorse/lhctl@0.7.2
```
  </TabItem>
</Tabs>

## LittleHorse Server (Local Dev)

The easiest way to run the LittleHorse Server is using the `lh-standalone` docker image. You can do so as follows:

```
docker run --name littlehorse -d -p 2023:2023 -p 8080:8080 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:0.7.2
```

Note that the `lh-standalone` image is only suitable for local development. Once the container is up and running, you should be able to verify that the server is working as follows:

```
-> lhctl version
lhctl version: 0.7.2
Server version: 0.7.2
```

:::info
The `lh-standalone` image takes some time to start up since it first starts a Kafka Broker process before starting the LH Server process. If your `lhctl search wfSpec` command fails at first, keep trying for about 20-40 seconds until Kafka is ready.
:::

You should be able to access the LH Developer Dashboard at `http://localhost:8080/` after the `lh-standalone` image has started up.

## LittleHorse Client Libraries

All the LittleHorse Client libraries, including grpc clients, can be accessed through standard installation mechanisms in a language of your choice.

<Tabs>
  <TabItem value="java" label="Java" default>

The Java Client Library is available on Maven Central. To add it as a dependency to your project, simply put the following in your `build.gradle` (or do the equivalent with your `pom.xml`)

```
  implementation 'io.littlehorse:littlehorse-client:0.7.2'
```
  </TabItem>
  <TabItem value="go" label="Go">
You add the LittleHorse Go Library as a dependency to your Go project as follows:

```
go get github.com/littlehorse-enterprises/littlehorse/sdk-go@0.7.2
```
  </TabItem>
  <TabItem value="python" label="Python">
You can install the LittleHorse Client Library as follows:

```
pip3 install littlehorse-client==0.7.2
```
  </TabItem>
</Tabs>

## Get Started

You can use one of the LittleHorse QuickStarts to get going really fast:

* **Java Quickstart** on [GitHub](https://github.com/littlehorse-enterprises/lh-quickstart-java) and on [YouTube](https://www.youtube.com/watch?v=8Zo_UOStg98)
* **Go Quickstart** on [GitHub](https://github.com/littlehorse-enterprises/lh-quickstart-go) and on [YouTube](https://www.youtube.com/watch?v=oZQc2ISSZsk)
* **Python Quickstart** on [GitHub](https://github.com/littlehorse-enterprises/lh-quickstart-python) and on [YouTube](https://www.youtube.com/watch?v=l3TZOjfpzTw)
* [**Join Our Slack**](https://launchpass.com/littlehorsecommunity)

Additional rich examples can be found at our [core GitHub repository](https://github.com/littlehorse-enterprises/littlehorse).
