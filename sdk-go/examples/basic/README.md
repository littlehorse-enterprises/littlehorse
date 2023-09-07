# Basic Example

This is a minimal "hello-world" example of LittleHorse using GoLang.

## What's Going On

The file [workflow.go](./workflow.go) has two functions defined:

- `Greet`, which is a Task Function.
- `MyWorkflow`, which is the Workflow Function.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`. The easiest way to do that is to run the task worker:

```
go run ./basic/worker
```

Leave that process running.

## Register the `WfSpec`

Next, in another terminal, run:

```
go run ./basic/deploy
```

That will create the `WfSpec`. You can verify that via:

```
lhctl get wfSpec my-workflow
```

## Run a `WfRun`

To run a `WfRun`, you can use `lhctl`.

```
lhctl run my-workflow name obi-wan
```

It will print out an ID. You can view the status of that `WfRun` via:

```
lhctl get wfRun <the id from the previous step>
```
