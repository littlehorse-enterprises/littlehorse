# Tips, Pointers, and FAQ

We do our best to make our documentation easy to navigate; however, LittleHorse has a lot of features and it may not always be easy to find what you are looking for.

Think of this page as an "index" that will help you find what you need to make your project a success.

## Workflow Basics

Read here for how to get started.

### What is a `WfSpec` and `WfRun`?

The core concept of LittleHorse is a [Workflow](./04-concepts/01-workflows.md), which defines a series of tasks to execute and events to wait for. A `WfSpec` defines a workflow specification. A `WfRun` is a running instance of that `WfSpec`.

### How do I create a `WfSpec`?

Check out our [`WfSpec` development docs](./05-developer-guide/08-wfspec-development/)! In short, you can use our Java, Python, or Go SDK's to define the `WfSpec` logic and then register them to the LH Server.

### What is a Task Worker?

A [Task Worker](./04-concepts/03-tasks.md) is a program that opens a connection to a LittleHorse Cluster and listens to a task queue for a specific `TaskDef`. When a `WfRun` arrives at a point where it needs to execute that specific type of `TaskRun`, then the scheduled Task will be dispatched to the Task Worker. The Task Worker executes it and reports the result to the LH Server.

### How do I write a Task Worker?

Check out our [Task Worker Development Docs](./05-developer-guide/05-task-worker-development.md)!

### How do I run a Workflow?

You can run a workflow in two ways:
* On the command line by [using `lhctl`](./05-developer-guide/03-lhctl.md#run-a-wfrun)
* Programmatically by [using one of our SDK's](./05-developer-guide/09-grpc/10-running-workflows.md).

## Variables and Control Flow

### How do I define variables in a Workflow?

Check out the [Variables Section](./05-developer-guide/08-wfspec-development/01-basics.md#defining-variables) of our `WfSpec` docs.

### Can I search for workflows by their variables?

Yes, you can. To do that, [mark the variable as `searchable()`](./05-developer-guide/08-wfspec-development/01-basics.md#searchable-and-required-variables) and then use [`rpc SearchVariable](./08-api.md#rpc-searchvariable) to search for it.

### How do I pass information from one task to another?

Variables are how you pass info from one task to another. Take the output of one task and `ASSIGN` it to a variable, then pass that variable into the next task. Check out our [mutating variables documentation](./05-developer-guide/08-wfspec-development/03-mutating-variables.md).

### How do I do if/else in a Workflow?

Check out the [Conditionals Section](./05-developer-guide/08-wfspec-development/02-conditionals.md) of our `WfSpec` docs.

### Can a workflow have loops?

Yes, check out the [loops section](./05-developer-guide/08-wfspec-development/02-conditionals.md#while-loops).

## External Events

### What is an `ExternalEvent`?

[External Events](./04-concepts/04-external-events.md) in LittleHorse represent events that occur outside of the context of a single `WfRun`. For example, you might use an `ExternalEvent` to notify a `WfRun` when someone signs a DocuSign document, or when a text message is replied to.

An `ExternalEvent` is an instance of an `ExternalEventDef`.

### How do I make a `WfRun` wait for an event?

Check out the [External Events](./05-developer-guide/08-wfspec-development/04-external-events.md) section of our `WfSpec` docs. What you want is an `ExternalEventNode`, which blocks until an `ExternalEvent` arrives.

### How do I interrupt a `WfRun`?

Check out the [Interrupts](./05-developer-guide/08-wfspec-development/05-interrupts.md) section of our `WfSpec` docs. What you want to do is register an Interrupt Handler for a certain `ExternalEventDef.`

### What happens when a `WfRun` is interrupted?

The interrupted `ThreadRun` is `HALTED`, and a child `ThreadRun` is created to act as the Interrupt Handler. For info, check out our [concept docs](./04-concepts/04-external-events.md#interrupts) and our [`WfSpec` development docs](./05-developer-guide/08-wfspec-development/05-interrupts.md).

### How can I access the content of an `ExternalEvent` inside my workflow?

You can use the output of an `ExternalEventNode` just like any other node in our SDK. Check out [these docs](./05-developer-guide/08-wfspec-development/04-external-events.md#external-events#accessing-event-content)

### How do I post an `ExternalEvent`?

You need to know the `WfRunId` of the `WfRun` you want to notify, and you need to know the `ExternalEventDefId`.

### Can I make `ExternalEvent`s idempotent?

Yes, just pass the `guid` parameter in the `PutExternalEventRequest` when making the `rpc PutExternalEvent`.

## Workflow Threading

### Can I run tasks in parallel?

To run tasks in parallel within a single `WfRun`, you need to [create a Child Thread](./05-developer-guide/08-wfspec-development/07-child-threads.md).

### Can different threads share variables?

A child `ThreadRun` can read and write the variables of its parent. A parent cannot view the variables of its child.

### What happens when a child fails?

When a child fails, the failure propagates to the parent either at:
* The `WaitForThreadsNode`, or
* The `ExitNode`.

See the [Child Thread Docs](./05-developer-guide/08-wfspec-development/07-child-threads.md) for info on how to handle these failures.

### What happens to the child when a parent fails?

When a parent `ThreadRun` fails (`ERROR` or `EXCEPTION`) or moves to `HALTED`, the child will move to `HALTED`.

## User Tasks

### What are User Tasks?

[User Tasks](./04-concepts/05-user-tasks.md) allow a workflow to wait for input from a human.

### How do I add a user task to my workflow?

Check out the [User Tasks](./05-developer-guide/08-wfspec-development/08-user-tasks.md) page of our `WfSpec` docs.

### Do User Tasks support users and user groups?

Yes, user tasks can be assigned to either a user_id or a user_group. Note that user and group identities are not managed by the LH Server.

### How do I complete User Tasks?

For testing purposes, you can do it [using `lhctl`](./05-developer-guide/03-lhctl.md#usertaskrun). For production applications, check out our docs on [managing User Tasks with grpc](./05-developer-guide/09-grpc/20-user-tasks.md).

### How do I create a `UserTaskDef`?

Check out our [Managing Metadata docs](./05-developer-guide/09-grpc/05-managing-metadata.md#usertaskdef).

## Exception Handling

### What is the difference between `ERROR` and `EXCEPTION`?

As per our [Failure Handling Concepts Docs](./04-concepts/01-workflows.md#failure-handling), an `ERROR` represents a technical failure such as a type error, network outage, timeout, or unexpected exception thrown by a task worker. An `EXCEPTION` represents something going wrong _at the business process level_, such as an invalid credit card or an item being out of stock.

### How do I catch a failure in a workflow?

Check out our [Failure Handling `WfSpec` docs](./05-developer-guide/08-wfspec-development/06-exception-handling.md).

### How do I throw an `EXCEPTION`?

You can [throw an `EXCEPTION` from a Task Worker](./05-developer-guide/05-task-worker-development.md#throwing-workflow-exceptions). You can also make a `ThreadRun` fail with the `WorkflowThread.fail()` method.

## Reliability

### Is LittleHorse Fault-Tolerant?

Yes, it was built into the [core DNA of our system](./02-architecture-and-guarantees.md).

### Does LittleHorse scale?

Yes.

### How do I make Workflows idempotent?

You can [pass the `WfRunId`](./05-developer-guide/09-grpc/10-running-workflows.md#passing-the-id) when running a `WfRun`. Only one `WfRun` can exist with a given Id, so this makes the request idempotent.

### How do I make Tasks idempotent?

You can use the [`WorkerContext`](./05-developer-guide/05-task-worker-development.md#idempotence) to get an idempotency key.

## About the Project

### Who Holds the Copyright?

The source code for LittleHorse is copyright of LittleHorse Enterprises LLC, a Nevada LLC.

### Can I use LittleHorse for free in production?

LittleHorse is covered by the [SSPL 1.0 License](https://www.mongodb.com/legal/licensing/server-side-public-license). You may run LittleHorse in production for free and with no restrictions so long as you are not offering LittleHorse-as-a-Service. Basically, if your customers are not using the LittleHorse GRPC clients to interact with LittleHorse directly, you can run LittleHorse for free in production.

The SSPL license was originally created by Mongo DB, who has an excellent [FAQ](https://www.mongodb.com/legal/licensing/server-side-public-license/faq) about the license terms.

### Can I get enterprise support?

Yes. [LittleHorse Enterprises LLC](https://littlehorse.io), which is the company behind the LittleHorse workflow engine, offers three forms of support for LittleHorse:

* 24/7 enterprise support for LittleHorse OSS.
* LittleHorse Cloud, which is a fully-managed SaaS service for LittleHorse.
* LittleHorse for Kubernetes, which is an enterprise distribution of LittleHorse delivered through Kubernetes Operator into your K8s cluster.

You can reach LittleHorse Enterprises LLC via their [website](https://littlehorse.io).

### Do you accept contributions?

Yes, we do accept contributions. Check out our [GitHub](https://github.com/littlehorse-enterprises/littlehorse) for tips on how to contribute.
