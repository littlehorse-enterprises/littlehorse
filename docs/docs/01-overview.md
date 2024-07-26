# LittleHorse Overview

The LittleHorse Server is a high-performance platform for building workflow-driven applications for a variety of use-cases, including:

- Business Process Management
- Logistics Automation
- Financial Transactions
- SAGA Transactions
- Event-Driven Microservices
- And more.

Building applications on LittleHorse enables engineering teams to save on infrastructure costs, reduce time to market, and deliver more robust software with less downtime.

The code for the LittleHorse Server and all clients is available at [our github](https://github.com/littlehorse-enterprises/littlehorse), but if you want to get started we recommend you check out our [Quickstart](./05-developer-guide/00-install.md). All code is free for production use under the Server-Side Public License. 

## How it Works

An application built on LittleHorse has the following three components:

- `WfSpec` Workflow Definition
- LittleHorse Server
- Task Workers

The `WfSpec`, short for Workflow Specification, is a metadata object that tells the LittleHorse Server which Tasks to schedule and when they should be scheduled. The Task Workers connect to the LittleHorse Server and execute Tasks as they are scheduled.

You as the user of LittleHorse define your own `WfSpec`s, and write your own Task Workers which connect to the LH Server.

A depiction of where LittleHorse sits in the tech stack can be seen below:

![Depiction of LittleHorse Stack](./littlehorse-in-the-stack.png)

You can think of LittleHorse as Middleware software that sits in the same layer as your database. End-user software applications occupy the layer above LittleHorse and _use_ LittleHorse to support a vertical business process.

### Run an App

To build an application with LittleHorse, there are three easy steps:

1. Define your `WfSpec`
2. Develop your Task Workers
3. Run your `WfRun`!

#### Define Your `WfSpec`

In LittleHorse, a `WfSpec` is a template that defines the logical steps for a certain process or workflow. It contains a set of steps (`Node`s), and flow between those steps (`Edge`s). For example, in an e-commerce application, you might write a `WfSpec` to orchestrate your checkout process:

1. Update the status of the `Order` in your database.
2. Reserve items in your inventory.
3. Charge the customer's credit card.
4. Notify the customer that the order was completed.

If step #2 or #3 fails, then your `WfSpec` would have logic to update the `Order` to failed and notify the customer accordingly.

`WfSpec`s can be developed in code, using our Java, Go, or Python SDK's. We have [extensive documentation](./05-developer-guide/08-wfspec-development/08-wfspec-development.md) for building `WfSpec`s.

#### Write Task Workers

A Task Worker is a computer system which connects to LittleHorse, listens to a task queue, and executes a [`TaskRun`](./04-concepts/03-tasks.md) as necessary. In the above e-commerce example, you would have a Task Worker for each step in the `WfSpec`.

It is simple to develop a Task Worker. Depending on which language you choose, you only need to write a handful of lines of code to integrate existing systems with LittleHorse as a Task Worker. Basically, any normal method or function can be converted to a LittleHorse Task Worker with no modifications. See our [documentation](./05-developer-guide/05-task-worker-development.md) for how to develop Task Workers.

#### Run a `WfRun`

Once your `WfSpec` is defined, and your Task Workers are polling for `TaskRun`s to execute, all you need to do is run your `WfRun`! You can test it out with our CLI, or build a production-ready API that uses our grpc client and executes the `RunWf` grpc call. See [our grpc docs](./05-developer-guide/09-grpc/09-grpc.md) for how to run a workflow.

## Features

At LittleHorse, we spent almost two years developing a cutting-edge system from the ground up so that we could provide you with a platform to future-proof your applications. Best of all, LittleHorse's source code is available and free for production use under the SSPL.

### Connect to Anything

LittleHorse has clients in Java, Go, and Python. This allows you to easily integrate existing systems into a LittleHorse Workflow with minimal code change (in Java, for example, all you need to do to turn a Method into a LittleHorse Task Worker is add the `@LHTaskMethod` annotation).

Additionally, LittleHorse supports integration with external systems through the [External Event](./04-concepts/04-external-events.md) feature, which allows LittleHorse Workflows to respond to events originating outside of the LittleHorse ecosystem.

### Support Mission-Critical Workflows

All data in LittleHorse Cloud is synchronously replicated into three separate datacenters (for example, AWS Availability Zones). LittleHorse has automated failover such that, if a server or even an entire datacenter fails, your workloads will promptly resume making progress on another data center (failover can be as low as 20 seconds).

Due to the synchronous nature of LittleHorse replication, we support an RPO of zero (no data loss) when failing over due to a server crash.

### Highly Secure

LittleHorse natively implements OAuth, TLS, and mTLS for authentication and encryption. LittleHorse also supports fine-grained ACL's to further lock down the system. Additionally, LittleHorse can be deployed into your own infrastructure (on-prem, private, or public cloud) so that no data leaves your four walls.

### High Performance

The LittleHorse Scheduler can scale horizontally to dozens or hundreds of servers. With only a handful of LH Server `Pod`s utlizing a total of just 48 cores on AWS EKS, we were able to schedule over 15,000 tasks per second. We are confident that LittleHorse can scale to meet the demands of any customer's use case.

Additionally, LittleHorse is high-performance system that introduces minimal delay. The latency between a Task being completed and the next Task being scheduled can be as low as 12-20ms; for comparison, a leading competitor's delays can reach 100-300ms.

### User Tasks

LittleHorse has rich and native support for assigning work to humans as well as computers. With the [User Tasks](./04-concepts/05-user-tasks.md) feature, a Workflow can wait for a human to provide input, and then immediately resume and take action based on the result provided by the human. User Tasks support a variety of useful features, including:
* Assignment to users or groups of users
* Automatic reassignment based on deadlines
* Reminder notifications
* Rich API's for querying and executing User Tasks.

## What LittleHorse is NOT

As the creator of LittleHorse, we believe that LittleHorse can help almost any IT organization. However, LittleHorse is able to excel at workflow-driven applications because we consciously decided _not_ to solve certain problems, listed below.

### Fully-Featured Database

LittleHorse is a special type of workflow engine for building workflow-driven applications and microservices. LittleHorse has a persistent storage layer which can be queried via the grpc API. It also has indexes which allow you to search for workflows based on the value of certain variables (eg. "find the workflow with `email == foo@bar.com`), workflow status (eg. "find all failed workflows in the last 2 days"), and workflow type. It is indeed possible to build a REST API using only LittleHorse as a data stores; in fact, it is often a good idea.

However, LittleHorse's persistence layer is _not_ a general-purpose database. It lacks the rich feature set of SQL, and you cannot use LittleHorse as a store for arbitrary data structures. If your data model is not a workflow, LittleHorse cannot store it.

### Vertical Solution

LittleHorse is intended to be used by Software Engineers _in any industry_ to build products that can be used by end-users. The current iteration is _not_ intended as a system to be used by Business Users to update business flows; rather, it is intended to _reduce the turnaround time_ when a Business User asks the IT Organization to implement a new application to automate a specific business flow.

However, LittleHorse has a certified implementation partner who can deliver a turnkey, end-to-end BPM solution to clients who require BPM solutions but do not have the engineering resources to implement one on their own.

### A Container Runtime

In the past, we described LittleHorse as a "microservice orchestration engine" because it orchestrates the activities of various microservices in a business flow. However, we no longer use that description since it causes confusion with Kubernetes, Nomad, Mesos, and other similar systems.

Rather than being a competitor or replacement for the aforementioned runtime systems, LittleHorse is unopinionated about where your code runs. LittleHorse (and especially LittleHorse Cloud) is compatible with any deployment system; all you need to do is provide the LittleHorse Server URL to your software system and it is LittleHorse-enabled, no matter where it runs.

### API Management System

LittleHorse is not an API Management System like Apigee or Kong.

### ETL System

LittleHorse is designed for dynamic, low-latency business process flows and not for bulk batch-loading flows or data transformation flows. We have a rich feature set which makes LittleHorse fantastic for orchestrating and debugging live (low-latency) workflow-driven applications and business process flows; however, LittleHorse's design lends itself less well for batch ETL and data ingestion + transformation.

For batch orchestration, we recommend systems like Airflow ([Astronomer](https://astronomer.io) has a fantastic Cloud Service for Airflow).

For data ingestion, consider systems like [Kafka](https://kafka.apache.org) and Kafka Connect; for data transformation and processing we would recommend considering [Kafka Streams](https://kafka.apache.org/documentation/streams/) or (if you need to process data outside of Kafka) [Flink](https://flink.apache.org).

### Service Mesh

To say that LittleHorse is "a system that intelligently routes tasks between your microservices" is mostly correct, but it is vague enough to mislead. In particular, you might ask, "doesn't a service mesh like Istio or a proxy like Envoy do the same?".

Service mesh implementations such as [Istio](https://istio.io) provide many awesome features, but the two most common use-cases are securing traffic at the L4 layer (mTLS) and request routing, for example with a [`VirtualService`](https://istio.io/latest/docs/reference/config/networking/virtual-service/).

LittleHorse sits _above_ the Istio layer. The LittleHorse Server is a server that LittleHorse Clients (eg. Task Workers and your Microservices) connect to. The Task Workers listen to virtual "task queues" within LittleHorse, and the LittleHorse Server dispatches [Tasks](./04-concepts/03-tasks.md) to the Task Workers according to the workflows.

:::note
When LittleHorse Cloud reaches general availability, we will _internally_ use Istio (as an implementation detail) in order to help secure traffic within our own clusters. We mention this because:

1. It illustrates that LittleHorse and Istio solve very different problems,
2. LittleHorse is compatible with Istio, and
3. We take security very seriously; Istio is one of many security layers we leverage in order to protect your data in LittleHorse Cloud.
:::

## Get Started

Want to painlessly automate your workflows?

* Check out our [Quickstarts](./05-developer-guide/00-install.md)
* Join our [Slack Community](https://launchpass.com/littlehorsecommunity)
* Check out our code on [Github](https://github.com/littlehorse-enterprises/littlehorse)
* Request early access to [LittleHorse Cloud](https://docs.google.com/forms/d/e/1FAIpQLScXVvTYy4LQnYoFoRKRQ7ppuxe0KgncsDukvm96qKN0pU5TnQ/viewform?usp=sf_link)

Ride well!