# Task Workers

A `WfSpec` defines a series of steps in a process to be orchestrated by LittleHorse (in technical terms, each step is a `Node`). The most common type of step to execute is a `TASK` node.

## Executing Tasks

A Task Worker is a program written by a LittleHorse user that connects to the LH Server, polls for `ScheduledTask`s to execute, executes the Tasks, and returns the result to LittleHorse. Task Workers can be easily developed using the Task Worker SDK's in Java, GoLang, and Python according to our documentation [here](../05-developer-guide/05-task-worker-development.md).

When a `ThreadRun` arrives at a `TaskNode`, the LittleHorse Server puts a `ScheduledTask` on a virtual Task Queue. The `ThreadRun` blocks at that `NodeRun` until a Task Worker pulls the `ScheduledTask` off the Task Queue, executes it, and reports the result (or if the configured timeout expires).

Once the `TaskRun` is completed, the output from the Task Worker's method invocation can be used by the workflow to mutate variables and control the flow of execution.

Task Workers open a web connection to the LittleHorse Server and do not need to receive connections on any ports. This has several benefits:

* Added security, since your systems do not need to accept any incoming connections.
* Built-in throttling, since the LittleHorse Server only dispatches a `ScheduledTask` to a Task Worker once the worker notifies the LH Server that it has capacity to perform a task.
* Performance, since the protocol is based on grpc bi-directional streaming.

### Task Logic

The work performed by Task Workers can be incredibly diverse, ranging from charging a customer's credit card to fetching data from an API to deploying infrastructure (as in a devops pipeline). Our Java, Go, and Python SDK's provide utilities that allow you to easily convert a function or method into a Task Worker in five lines of code or less.

In short, a Task Worker can perform any arbitrary function in code. However, we recommend using Task Workers for processes that take on the order of seconds rather than minutes or hours. For longer-running tasks, we recommend using a TaskRun to kick the process off and an `ExternalEvent` to note its completion.

### Deploying a Task Worker

A Task Worker is any program that uses the Task Worker SDK's to execute `TaskRun`'s. LittleHorse is not opinionated about where or how the Task Worker is deployed: it can be a JVM process running on a bare metal server under a desk; it can be a docker container on ECS, or a Pod in Kubernetes.

Additionally, a single process can run multiple Task Workers for different `TaskDef`'s at once. This is often useful if you want to take advantage of workflow-driven processes but you have no need for microservices and as such want to avoid managing multiple deployable artifacts.
