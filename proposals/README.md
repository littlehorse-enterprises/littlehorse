# LittleHorse Kernel Proposals

This directory contains proposals for significant changes to the LittleHorse Kernel.

## About Proposals

Proposals are similar to Kafka Improvement Proposals (KIP's) in Apache Kafka and [Strimzi Proposals](https://github.com/strimzi/proposals) in Strimzi. Proposals should be written before significant features which have significant impact on:

* Any changes to the Protocol Buffer API (in the [`schemas` directory](../schemas/)).
* Significant changes to the SDK API in Java, Go, Python, or C#.
* Significant changes to the internal architecture of the Server which might affect performance.

Proposals should include:

* The motivation for the new feature or change.
* Any proposed Protocol Buffer changes.
* Any proposed changes to the SDK API.
* Any architectural changes to the Server which might impact performance or operations.
* How the Proposal will affect backwards compatibility, deprecations, etc.

To make a Proposal, please open a PR which adds the Proposasl markdown file and adds it to the `Accepted Proposals` table below.

## Accepted Proposals

| #  | Title                                                                 |Author(s)    |
|:--:|:----------------------------------------------------------------------|-------------|
| 000 | [`Struct` and `StructDef](./000-struct-and-structdef.md) |Colt McNealy |