---
slug: basics-of-workflow
authors:
- mitchellh
tags: [analysis,littlehorse]
---

# The Basics of Workflow

LittleHorse Enterprises is a workflow engine company. But what is a workflow engine?

<!-- truncate -->

It is a system that allows you to reliably execute a series of steps while being robust to technical failures (network outages, crashes) and business process failures. A step in a workflow can be calling a piece of code on a server, reaching out to an external API, waiting for a callback from a person or external system, or more.

A core challenge when automating a business process is **Failure and Exception Handling:** figuring out what to do when something doesn't happen, happens with an unexpected outcome, or plain simply fails. This is often difficult to reason about, leaving your applications vulnerable to uncaught exceptions, incomplete business workflows, or data loss.

A workflow engine standardizes how to throw an exception, where the exception is logged, and the logic around when/how to retry. This gives you peace of mind that once a workflow run is started, it will reliably complete.

## Workflow Architecture

Any [workflow-driven application](https://littlehorse.dev/docs/concepts) has three components:

1. A really awesome workflow engine like LittleHorse.
2. A [Workflow Specification](https://littlehorse.dev/docs/concepts/workflows), which defines the series of steps in your application.
3. [Task Workers](https://littlehorse.dev/docs/concepts/tasks), which are computer programs that execute work when the LH Server tells it to.

### Workflow Specifications

A Workflow Specification (or `WfSpec` in LittleHorse) is the configuration, or metadata object, that tells the engine what Tasks to run,
what order to run the tasks, **how to handle exceptions or failures,** what variables are to be passed from task to task, and what inputs and outputs are required to run the workflow.

In LittleHorse the `WfSpec` is submitted to and held by the LittleHorse server. Users of LittleHorse can define a `WfSpec` in vanilla code (Java/Go/Python) using the LittleHorse SDK. The SDK will compile your vanilla code into a `WfSpec` that the LH Server understands and keeps inside its data store.

:::info
To learn how to write a `WfSpec` in LittleHorse, check out our [`WfSpec` Development docs](https://littlehorse.dev/docs/developer-guide/wfspec-development).
:::

In the background LittleHorse server takes the submitted spec from the SDK, and compiles a protobuf object that is submitted to the LittleHorse server.

For example, the following code in Java defines a two-step workflow in which we look up the price of an item, charge a customer's credit card, and then ship an item.

```java
public class ECommerceWorkflow {

    public void checkoutWorkflow(WorkflowThread wf) {
        // Create some Workflow Variables
        var customerId = wf.addVariable("customer-id", VariableType.STR).searchable().required();
        var itemId = wf.addVariable("item-id", VariableType.STR).required();
        var price = wf.addVariable("price", VariableType.INT);

        // Fetch Price and save it into a variable
        var priceOutput = wf.execute("calculate-price", itemId);
        wf.mutate(price, VariableMutationType.ASSIGN, priceOutput);

        // Charge credit card (passing in the output from previous task)
        wf.execute("charge-credit-card", customerId, price);

        // Ship item
        wf.execute("ship-item", customerId, itemId);
    }
}
```

:::note
Just by using LittleHorse to define the above workflow, you get reliability, observability, retries, and governance out of the box!
:::

### Tasks and Task Workers

Tasks are the unit of work that can be executed a workflow engine. It's best to think in examples:
* Change lower case letters to upper case letters.
* Call an API with an input variable and pass along the output.
* Fetch data from a database.
* Convert a message from HL7 version 2.5 to HL7 version 3.

Task workers are programs that use the LittleHorse SDK, connect to LittleHorse, and execute tasks when the workflow says it's time to do so.

:::tip
To learn how to write a Task Worker, check out our [Task Worker Development Guide](https://littlehorse.dev/docs/developer-guide/task-worker-development).
:::

You can also use [External Events](https://littlehorse.dev/docs/concepts/external-events) or [User Tasks](https://littlehorse.dev/docs/concepts/user-tasks) to wait for input from a human user or an external system (like a callback or webhook).


### Workflow Clients

Lastly you need to tell LittleHorse when to run a workflow. You can do it with our CLI (`lhctl`) but in production you'll need to use the LittleHorse SDK to kick off a workflow. You can do this with our page on [Running Workflows using grpc](https://littlehorse.dev/docs/developer-guide/grpc/running-workflows)

You'll also need to tell LittleHorse about External Events that happen. You can also do this using `lhctl` or [with our SDK's](https://littlehorse.dev/docs/developer-guide/grpc/posting-external-events).

## LittleHorse Use-Cases

There are many different types of workflow engines, each of which supports different use-cases. For example:

* **Batch ETL and Cronjob** workflows are automated by systems like Apache Airflow and Dagster.
* **Infrastructure Provisioning and Configuration** workflows can be automated by Ansible, Argo, and Jenkins.
* **IT Integration and BPM** workflows may be automated by systems like Camunda and jBPM.

However, **LittleHorse allows you to orchestrate business processes across your software systems.** Some use-cases are included below.

### Microservices

All microservice-based applications are inherently distributed systems with the goal of supporting some business process (because no one writes microservices for the sake of writing code, right?). While often necessary, microservices [present many challenges](https://littlehorse.dev/blog/challenge-of-microservices) to developers due to their distributed nature.

Our founder Colt McNealy wrote a [detailed blog](https://littlehorse.dev/blog/microservices-and-workflow) about how a workflow engine's reliabile state management and oversight can mitigate some of the problems inherent in microservices. Check it out!

### Human-in-the-Loop

Workflows often need to get input from humans:
* Approval flows.
* Waiting for information from customers.
* Handling exceptional scenarios.

That's hard to coordinate without a workflow engine. You'd have to build your own state management system that correlates tasks to workflows. LittleHorse [User Tasks](https://littlehorse.dev/docs/concepts/user-tasks) make this much easier.

### RAG and AI

AI is only useful when you call it at the right time, with the right inputs, and do something with the outputs. That's a workflow. And all sorts of things can go wrong when using LLM's, which is why you need to have a robust workflow engine to provide oversight and exception handling.

### Legacy System Modernization

Whether you are integrating legacy systems that you inherited from the past, or integrating multiple tech stacks accrued through M&A, your customers expect a real-time experience that seamlessly spans all of your systems. Workflow engines are useful for reliably orchestrating actions and moving data across multiple different systems.

### API gateway

If we look at the properties of an API gateway and how they are used, a workflow engine makes sense.  
<!--TODO: insert example API gateway architecture -->
The usage of an API gateway is to have a single layer that abstracts further endpoints.  
In practice this most often means calling the same API gateway multiple times, receiving the requested data, and doing some date manipulation or calculations at the application layer.
A workflow engine performs all of the most common actions, and includes things like centralized security, possible data obscurity, failure handling, observability and allows for operators to scale compute.
All while still maintaining a central plane that can be shared across an entire orginization.  
Additionally a workflow engine still allows for the standard CRUD(Create, Read, Update, Delete) operations that an API gateway provides.
