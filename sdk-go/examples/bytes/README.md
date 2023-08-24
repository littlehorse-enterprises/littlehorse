# Bytes Example

This is a "hello-world" with bytes example of LittleHorse using GoLang.

## What's Going On

The file [workflow.go](./workflow.go) has two functions defined:

- `Greet`, which is a Task Function.
- `MyWorkflow`, which is the Workflow Function.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`. The easiest way to do that is to run the task worker:

```
go run ./bytes/worker
```

Leave that process running.

## Register the `WfSpec`

Next, in another terminal, run:

```
go run ./bytes/deploy
```

That will create the `WfSpec`. You can verify that via:

```
lhctl get wfSpecModel my-workflow
```

## Run a `WfRun`

To run a `WfRun`, you can use `lhctl`.

```
lhctl run my-workflow
```

It will print out an ID. You can view the status of that `WfRun` via:

```
lhctl get wfRunModel <the id from the previous step>
```
