---
sidebar_label: Task Workers
---
# Task Worker

A Task Worker is a program written by a LittleHorse user that connects to the LH Server, polls for `ScheduledTask`s to execute, executes the Tasks, and returns the result to LittleHorse. Task Workers can be easily developed using the Task Worker SDK's in Java, GoLang, and Python (support for C# coming soon).

The work performed by Task Workers can be incredibly diverse, ranging from charging a customer's credit card to fetching data from an API to deploying infrastructure (as in a devops pipeline).

Task Workers open a web connection to the LittleHorse Server and do not need to receive connections on any ports. This has several benefits:

* Added security, since your systems do not need to accept any incoming connections.
* Built-in throttling, since the LittleHorse Server only dispatches a `ScheduledTask` to a Task Worker once the worker notifies the LH Server that it has capacity to perform a task.
* Performance, since the protocol is based on grpc bi-directional streaming.
