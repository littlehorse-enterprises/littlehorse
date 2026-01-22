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
* The GitHub Issue used to track implementation progress.

To make a Proposal, please open a PR which adds the Proposasl markdown file and adds it to the `Accepted Proposals` table below.

## Accepted Proposals

| #  | Title                                                                 |Author(s)    |GitHub Issue #|
|:--:|:----------------------------------------------------------------------|-------------|--------------|
| 000 | [`Struct` and `StructDef`](./000-struct-and-structdef.md) |Colt McNealy and Jacob Snarr |[#880](https://github.com/littlehorse-enterprises/littlehorse/issues/880)|
| 001 | [The Output Topic](./001-output-topic.md) | Colt McNealy | [#1304](https://github.com/littlehorse-enterprises/littlehorse/issues/1304) |
| 002 | [Moving Towards Strong Typing](./002-move-to-strong-typing.md) | Colt McNealy | [#1543](https://github.com/littlehorse-enterprises/littlehorse/issues/1543) |
| 003 | [Native `RunWf` Nodes](./003-run-wf-node.md) | Colt McNealy | [#589](https://github.com/littlehorse-enterprises/littlehorse/issues/589) |
| 004 | [Comments on `UserTaskRun`s](./004-add-user-task-comments.md) | Jake Rose and Karla Carvajal | [#1376](https://github.com/littlehorse-enterprises/littlehorse/issues/1376) |
| 005 | [Correlated `ExternalEvent`s](./005-correlated-events.md) | Colt McNealy | [#1579](https://github.com/littlehorse-enterprises/littlehorse/issues/1579) |
| 006 | [`UserTaskEvent` Completed](./006-add-user-task-event-completed.md) | Jake Rose | [#904](https://github.com/littlehorse-enterprises/littlehorse/issues/904) |
| 007 | [`ExternalEvent`s `wfspec` and `wfRun` validations](./007-extending-external-event-validations.md) | Jake Rose | [#588](https://github.com/littlehorse-enterprises/littlehorse/issues/588) |
| 008 | [Type casting](./008-type-casting.md) | Christian Caicedo | [#1543](https://github.com/littlehorse-enterprises/littlehorse/issues/1543)|
| 009 | [Checkpointed Tasks](./009-checkpointed-task-run.md) | Colt McNealy | [#1765](https://github.com/littlehorse-enterprises/littlehorse/issues/1765) |
| 010 | [`ThreadRun` Archival](./010-archive-thread-run.md) | Jacob Snarr | TBD |
| 012 | [Conditions Refactor](./012-conditions-refactor.md) | Colt McNealy | TBD |