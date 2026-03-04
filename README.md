# LittleHorse

<a href="https://littlehorse.io/"><img alt="littlehorse.io" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/site-badge.svg"/></a>
<a href="https://littlehorse.io/docs/getting-started/quickstart"><img alt="littlehorse.io/docs/server/concepts" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/learn-badge.svg"/></a>
<a href="https://littlehorse.io/docs"><img alt="littlehorse.io/docs" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/docs-badge.svg"/></a>
[![slack](https://img.shields.io/badge/Slack-511651.svg?logo=slack)](https://launchpass.com/littlehorsecommunity/free)

<a href="https://central.sonatype.com/artifact/io.littlehorse/littlehorse-client"><img alt="java" src="https://img.shields.io/maven-central/v/io.littlehorse/littlehorse-client?logo=openjdk&logoColor=white&color=orange&label=java"></a>
<a href="https://pkg.go.dev/github.com/littlehorse-enterprises/littlehorse"><img alt="go" src="https://img.shields.io/github/v/release/littlehorse-enterprises/littlehorse?logo=go&logoColor=white&color=00aed8&label=go"></a>
<a href="https://pypi.org/project/littlehorse-client/"><img alt="python" src="https://img.shields.io/pypi/v/littlehorse-client?logo=python&logoColor=white&color=success&label=python"></a>


<p align="center">
<img alt="LH" src="./img/wfRun.png" width="80%">
</p>

[LittleHorse](https://littlehorse.io) is a high-performance microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. By allowing LittleHorse to manage coordination and sequencing of your applications, you no longer have to worry about:

* Wiring microservices together with RPC calls or message queues.
* Retries, timeouts, dead-letter queues.
* Distributed tracing and debugging across multiple microservices.
* Scheduling actions to asychronously happen in the future.
* Backpressure and scalability.


## Getting Started

### Installing LittleHorse

1. Install the LittleHorse CLI agent as follows:

```sh
brew install littlehorse-enterprises/lh/lhctl
```

Alternatively, you can install it from our [GitHub Releases page](https://github.com/littlehorse-enterprises/littlehorse/releases)

2. Next, run LittleHorse Server and Dashboard using our standalone docker image:

```
docker run --name littlehorse -d -p 2023:2023 -p 8080:8080 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

3. Verify the server is installed and running using lhctl

```
-> lhctl version
lhctl version: 0.16.0 (Git SHA homebrew)
Server version: 0.16.0
```

4. Navigate to the dashboard at `http://localhost:8080`

For more information go to our [Quickstart Installation Docs](https://littlehorse.io/docs/server/developer-guide/install).

### Running Your First Workflow

To run a workflow with LittleHorse, you need to:

- Define tasks which are units of work that can be used in a process, and implement programs that execute those tasks.
- Define your workflows and tell the workflow engine about it
- Run the workflow
- The workflow engine ensures that your process gets executed correctly.

<p align="center">
<img src="./img/architecture.png" width="75%">
</p>

To get started quickly with a basic workflow, try our quickstarts in [Java](https://github.com/littlehorse-enterprises/lh-examples/tree/main/quickstart/java), [Go](https://github.com/littlehorse-enterprises/lh-examples/tree/main/quickstart/go), [Python](https://github.com/littlehorse-enterprises/lh-examples/tree/main/quickstart/python), and [C#](https://github.com/littlehorse-enterprises/lh-examples/tree/main/quickstart/csharp). For more detailed examples, you can check out:
- The [examples directory](./examples) in this repo
- The [lh-examples repository](https://github.com/littlehorse-enterprises/lh-examples), which contains more complex applications.

For documentation, visit [littlehorse.io/docs/server](https://www.littlehorse.io/docs/server).

## About the Project

### Lifecycle and Release Plan

The LittleHorse Server plans to follow [Semantic Versioning](https://semver.org) after the release of version 1.0. You can find our (non-binding) project guidelines regarding our release schedule and deprecation strategy for after the 1.0 release in our [project lifecycle document](./PROJECT_LIFECYCLE.md). We plan to release version 1.0 in early 2026.

### Developing

For information about developing LittleHorse, see the guide in our [local-dev README](./local-dev/README.md).

### License

<a href="https://www.gnu.org/licenses/agpl-3.0.en.html"><img alt="AGPLv3 License" src="https://img.shields.io/badge/covered%20by-AGPLv3-blue"></a>

All code in the `./server` and `./dashboard` directories in this repository is licensed by the [GNU Affero General Public License, Version 3](https://www.gnu.org/licenses/agpl-3.0.en.html) and is copyright of LittleHorse Enterprises LLC.

All docker images from this repository are licensed by the [GNU Affero General Public License, Version 3](https://www.gnu.org/licenses/agpl-3.0.en.html) and are copyright of LittleHorse Enterprises LLC.

All other code and packages, including our SDK's, `lhctl`, examples, and the corresponding packages is licensed by the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
