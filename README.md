<p align="center">
<img alt="LH" src="https://littlehorse.dev/img/logo.jpg" width="50%">
</p>

# LittleHorse

<a href="https://littlehorse.dev/"><img alt="littlehorse.dev" src="https://img.shields.io/badge/-LittleHorse.dev-7f7aff"></a>

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

To get started *using* LittleHorse, check out the [Java Quickstart](docs/QUICKSTART_JAVA.md) or the [GoLang Quickstart](docs/QUICKSTART_GOLANG.md).

To get started *developing* LittleHorse, check out the [Development Guide](docs/DEVELOPING.md).

- [Server Configurations](docs/CONFIGURATIONS_SERVER.md)
- [Workers/Clients Configurations](docs/CONFIGURATIONS_CLIENTS.md)

## License

<a href="https://spdx.org/licenses/SSPL-1.0.html"><img alt="SSPL LICENSE" src="https://img.shields.io/badge/covered%20by-SSPL%201.0-blue"></a>

Source code in this repository is covered by the [Server
Side Public License version 1](https://spdx.org/licenses/SSPL-1.0.html), unless the header specifies another license. 