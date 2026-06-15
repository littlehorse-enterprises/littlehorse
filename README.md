<div align="center">
<h1>LittleHorse: Command Center for Business-as-Code</h1>

Define distributed processes in code, and let LittleHorse orchestrate, track, and govern them. 

<a href="https://littlehorse.io/"><img alt="littlehorse.io" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/site-badge.svg"/></a>
<a href="https://littlehorse.io/docs/getting-started/quickstart"><img alt="littlehorse.io/docs/server/concepts" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/learn-badge.svg"/></a>
<a href="https://littlehorse.io/docs"><img alt="littlehorse.io/docs" src="https://github.com/littlehorse-enterprises/.github/blob/master/assets/docs-badge.svg"/></a>
[![slack](https://custom-icon-badges.demolab.com/badge/Slack-4A154B?logo=slack&logoColor=fff)](https://launchpass.com/littlehorsecommunity/free)

<a href="https://central.sonatype.com/artifact/io.littlehorse/littlehorse-client"><img alt="java" src="https://img.shields.io/maven-central/v/io.littlehorse/littlehorse-client?logo=openjdk&logoColor=white&color=orange&label=java"></a>
<a href="https://pkg.go.dev/github.com/littlehorse-enterprises/littlehorse"><img alt="go" src="https://img.shields.io/github/v/release/littlehorse-enterprises/littlehorse?logo=go&logoColor=white&color=00aed8&label=go"></a>
<a href="https://pypi.org/project/littlehorse-client/"><img alt="python" src="https://img.shields.io/pypi/v/littlehorse-client?logo=python&logoColor=white&color=success&label=python"></a>
<a href="https://www.npmjs.com/package/littlehorse-client"><img alt="js" src="https://img.shields.io/npm/v/littlehorse-client?logo=javascript&logoColor=white&color=F7DF1E&label=js"></a>
<a href="https://www.nuget.org/packages/LittleHorse.Sdk"><img alt="dotnet" src="https://img.shields.io/nuget/v/LittleHorse.Sdk?logo=dotnet&logoColor=white&color=purple&label=dotnet"></a>
</div>

## About LittleHorse

[LittleHorse](https://littlehorse.io) is a high-throughput, low-latency microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. LittleHorse's _Business-as-Code_ approach allows you to write code that closely mirrors your business processes, creating better alignment between product & engineering while providing a robust durable execution platform.

Let LittleHorse take the reins and ditch the headaches of:

* Wiring microservices together with RPC calls or message queues.
* Managing the coordination and sequencing of your applications.
* Retries, timeouts, dead-letter queues.
* Distributed tracing and debugging across multiple microservices.
* Scheduling actions to asychronously happen in the future.
* Backpressure and scalability.

LittleHorse is built on Apache Kafka and Kafka Streams, and has [rich integrations](https://github.com/littlehorse-enterprises/lh-kafka-connect) with the Kafka Ecosystem.

## Business-as-Code

<img alt="LH" src="./img/wfRun.png" width="100%">

:point_up: This picture shows a running instance (`WfRun`) for the process (`WfSpec`) defined by this code :point_down:

```java
public void quickstartWf(WorkflowThread wf) {
    WfRunVariable fullName = wf.declareStr("full-name").searchable().required();
    WfRunVariable email = wf.declareStr("email").searchable().required();

    // Social Security Numbers are sensitive, so we mask the variable with `.masked()`.
    WfRunVariable ssn = wf.declareInt("ssn").masked().required();

    WfRunVariable identityVerified = wf.declareBool("identity-verified").searchable();

    wf.execute(VERIFY_IDENTITY_TASK, fullName, email, ssn).withRetries(3);

    NodeOutput identityVerificationResult = wf.waitForEvent(IDENTITY_VERIFIED_EVENT)
            .timeout(60 * 5) // 5 minute timeout
            .withCorrelationId(email)
            .registeredAs(Boolean.class);

    wf.handleError(identityVerificationResult, LHErrorType.TIMEOUT, handler -> {
        handler.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
        handler.fail("customer-not-verified", "Unable to verify customer identity in time.");
    });

    identityVerified.assign(identityVerificationResult);

    wf.doIf(identityVerified.isEqualTo(true), ifBody -> {
        ifBody.execute(NOTIFY_CUSTOMER_VERIFIED_TASK, fullName, email);
    })
    .doElse(elseBody -> {
        elseBody.execute(NOTIFY_CUSTOMER_NOT_VERIFIED_TASK, fullName, email);
    });
}
```

As you can see, the code above closely mirrors our example KYC business process. LittleHorse handles retries, timeouts, and orchestration across services for you, allowing your `WfSpec` to focus just on what matters to the business. Task workers handle integrations with external systems and databases.

## Getting Started

Run your first `WfRun` in 120 seconds or less.

### Start the LittleHorse Server

Run the LittleHorse Server and Dashboard using our standalone docker image:

```
docker run --rm --pull=always --name littlehorse -d -p 9092:9092 -p 2023:2023 -p 8080:8080 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:latest
```

> Note: if you want to play with the [output topic](./examples/java/output-topic/), which sends workflow updates to Kafka in real time, this also exposes a Kafka broker on `localhost:9092`.

### Install the LittleHorse CLI

```sh
brew install littlehorse-enterprises/lh/lhctl
```

Alternatively, you can install it from our [GitHub Releases page](https://github.com/littlehorse-enterprises/littlehorse/releases)

Once you have `lhctl` ready, let's use the `whoami` command to verify that the LittleHorse Server is up and running:

```sh
lhctl whoami
```
```json
{
  "id": {
    "id": "anonymous"
  },
  "createdAt": "2026-03-05T02:51:57.229Z",
  "perTenantAcls": {},
  "globalAcls": {
    "acls": [
      {
        "resources": [
          "ACL_ALL_RESOURCES"
        ],
        "allowedActions": [
          "ALL_ACTIONS"
        ],
        "name": ""
      }
    ]
  }
}
```

### Start a Quickstart

Start a quickstart in a language of your choice. Each command registers the workflow metadata and starts the task workers.

#### Java

```
./gradlew quickstart:run
```

#### Python

```
cd examples/python
poetry run python -m quickstart.quickstart
```

#### GoLang

```
go run ./examples/go/quickstart
```

#### C#

```sh
cd examples/dotnet/QuickstartExample
DOTNET_ROLL_FORWARD=Major dotnet run
```

#### JavaScript

First, install dependencies and start the task workers (this registers the required `TaskDef`s):

```sh
cd examples/js/quickstart
npm install
npm start
```

Then in another terminal, register the `WfSpec` (note that our JS SDK does not yet support creation of `WfSpec`s, so we use `lhctl` here):

```sh
cd examples/js/quickstart
lhctl deploy wfSpec quickstart-wfspec.json
```

### Run a `WfRun` (Workflow Run)

In another terminal, run the quickstart workflow:

```sh
lhctl run quickstart full-name 'Obi-Wan Kenobi' email obiwan@jedi.temple ssn 123456789
```
Now, navigate to the dashboard at [`http://localhost:8080`](http://localhost:8080) and inspect your first `WfRun`. You'll notice that it's blocked by the 

Then post the correlated event that unblocks the workflow:

```sh
lhctl put correlatedEvent obiwan@jedi.temple identity-verified BOOL true
```

See the per-language quickstarts for the full walkthrough:

- [Java quickstart](./examples/java/quickstart/README.md)
- [Go quickstart](./examples/go/quickstart/README.md)
- [Python quickstart](./examples/python/quickstart/README.md)
- [C# quickstart](./examples/dotnet/QuickstartExample/README.md)
- [JavaScript quickstart](./examples/js/quickstart/README.md)

You can also use `lhctl` to investigate! For starters:

* `lhctl get wfRun <wfRunId>`
* `lhctl get nodeRun <wfRunId> 0 1`
* `lhctl list taskRun <wfRunId>`

## Learn More

* Check out our [Concepts Documentation](https://littlehorse.io/docs/server/concepts)!
* Run more examples in our [examples](./examples/) directory.
* Join our [Slack Community](https://launchpass.com/littlehorsecommunity/free)

## Architecture

To run a workflow with LittleHorse, you need to:

- Define tasks which are units of work that can be used in a process, and implement programs that execute those tasks.
- Define your workflows and tell the workflow engine about it
- Run the workflow
- The workflow engine ensures that your process gets executed correctly.

<p align="center">
<img src="./img/architecture.png" width="75%">
</p>

To get started quickly with a basic workflow, try our quickstarts in [Java](./examples/java/quickstart/README.md), [Go](./examples/go/quickstart/README.md), [Python](./examples/python/quickstart/README.md), [C#](./examples/dotnet/QuickstartExample/README.md), and [JavaScript](./examples/js/quickstart/README.md). For more detailed examples, you can check out:
- The [examples directory](./examples) in this repo
- The [lh-examples repository](https://github.com/littlehorse-enterprises/lh-examples), which contains more complex applications.

For documentation, visit [littlehorse.io/docs/server](https://www.littlehorse.io/docs/server).

## About the Project

LittleHorse is developed with love by engineers, for engineers.

### Lifecycle and Release Plan

The LittleHorse Server follows [Semantic Versioning](https://semver.org) after the release of version 1.0. You can find our (non-binding) project guidelines regarding our release schedule and deprecation strategy in our [project lifecycle document](./PROJECT_LIFECYCLE.md).

### Developing

For information about developing LittleHorse, see the guide in our [local-dev README](./local-dev/README.md).

### License

<a href="https://www.gnu.org/licenses/agpl-3.0.en.html"><img alt="AGPLv3 License" src="https://img.shields.io/badge/covered%20by-AGPLv3-blue"></a>

All code in the `./server` and `./dashboard` directories in this repository is licensed by the [GNU Affero General Public License, Version 3](https://www.gnu.org/licenses/agpl-3.0.en.html) and is copyright of LittleHorse Enterprises LLC.

All docker images from this repository are licensed by the [GNU Affero General Public License, Version 3](https://www.gnu.org/licenses/agpl-3.0.en.html) and are copyright of LittleHorse Enterprises LLC.

All other code and packages, including our SDK's, `lhctl`, examples, and the corresponding packages is licensed by the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
