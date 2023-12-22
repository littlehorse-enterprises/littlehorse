<p align="center">
<img alt="LH" src="https://raw.githubusercontent.com/littlehorse-enterprises/littlehorse/master/docs/images/black-logo-500px.png" width="50%">
</p>

# LittleHorse

<a href="https://github.com/littlehorse-enterprises/littlehorse"><img alt="github" src="https://img.shields.io/badge/GitHub-blue?logo=github&logoColor=white"></a>
<a href="https://littlehorse.io/"><img alt="littlehorse.io" src="https://raw.githubusercontent.com/littlehorse-enterprises/littlehorse/master/docs/images/badge-littlehorse-io.svg"/></a>
<a href="https://littlehorse.dev/"><img alt="littlehorse.dev" src="https://raw.githubusercontent.com/littlehorse-enterprises/littlehorse/master/docs/images/badge-littlehorse-dev.svg"/></a>

<a href="https://central.sonatype.com/artifact/io.littlehorse/littlehorse-client"><img alt="java" src="https://img.shields.io/maven-central/v/io.littlehorse/littlehorse-client?logo=openjdk&logoColor=white&color=orange&label=java"></a>
<a href="https://github.com/littlehorse-enterprises/littlehorse/tags"><img alt="go" src="https://img.shields.io/github/v/tag/littlehorse-enterprises/littlehorse?logo=go&logoColor=white&color=00aed8&label=go"></a>
<a href="https://pypi.org/project/littlehorse-client/"><img alt="python" src="https://img.shields.io/pypi/v/littlehorse-client?logo=python&logoColor=white&color=success&label=python"></a>

[LittleHorse](https://littlehorse.dev) is a high-performance microservice orchestration engine that allows developers to build scalable, maintainable, and observable applications. The LittleHorse Runtime has uses in fields such as:

- Business Process Management
- Event-Driven Systems
- Logistics Management Applications
- Financial Transaction Processing
- And More.

## LH Repository Inventory

The LittleHorse repository contains the following components:

- [`server`](server)
    - Code for LittleHorse Server.
- [`sdk-java`](sdk-java)
    - Library for creating `WfSpec`'s in Java.
    - Library for executing `TaskRun`'s in Java.
- [`sdk-go`](sdk-go)
    - Library for creating `WfSpec`'s in GoLang.
    - Library for executing `TaskRun`'s in GoLang.
- [`sdk-python`](sdk-python)
    - Library for creating `WfSpec`'s in Python.
    - Library for executing `TaskRun`'s in Python.
- [`sdk-dotnet`](sdk-dotnet)
  - Library for creating `TaskRun`'s in .NET.
- [`dashboard`](dashboard)
  - Web UI for LH.
- [`lhctl`](lhctl)
    - The `lhctl` command line interface.
- [`e2e-tests`](e2e-tests)
    - A series of system integration tests used to verify the cohesive behavior of
    the LH Server, Java Workflow SDK, and Java Task Library together.
- [`examples`](examples)
    - A series of examples with different level of complexity.
- [`schemas`](schemas)
    - The LittleHorse Protocol Buffer Specification.
- [`docker`](docker)
    - The LittleHorse Docker Image.

## Getting Started

To get started *using* LittleHorse, check out the [Java Quickstart](docs/QUICKSTART_JAVA.md) or the [GoLang Quickstart](docs/QUICKSTART_GO.md). Also, if you want to run a docker sandbox go to the [Docker Quickstart](docs/QUICKSTART_DOCKER.md).

To get started *developing* LittleHorse, check out the [Development Guide](docs/DEVELOPING.md).

- [Server Configurations](docs/SERVER_CONFIGURATIONS.md)
- [Workers/Clients Configurations](docs/CLIENT_CONFIGURATIONS.md)
- [Dashboard Configurations](docs/DASHBOARD_CONFIGURATIONS.md)

## License

<a href="https://spdx.org/licenses/SSPL-1.0.html"><img alt="SSPL LICENSE" src="https://img.shields.io/badge/covered%20by-SSPL%201.0-blue"></a>

All code in this repository is covered by the [Server Side Public License, Version 1](https://spdx.org/licenses/SSPL-1.0.html). All code is intellectual property of LittleHorse Enterprises LLC.
