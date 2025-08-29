# JSON Array Example

This is a "hello-world" with a JSON Array example of LittleHorse using GoLang.

## What's Going On

The file [workflow.go](./workflow.go) has two functions defined:

- `AddUpList`, which is a Task Function (task accepts a slice as an input variable; only pointers to slice are supported).
- `MyWorkflowAdd`, which is the Workflow Function.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`. The easiest way to do that is to run the task worker:

```
go run ./examples/jsonarray/worker
```

Leave that process running.

## Register the `WfSpec`

Next, in another terminal, run:

```
go run ./examples/jsonarray/deploy
```

That will create the `WfSpec`. You can verify that via:

```
lhctl get wfSpec json-array-workflow
```

## Run a `WfRun`

To run a `WfRun`, you can use `lhctl`.

```
lhctl run json-array-workflow input '[{"foo": 1, "art":{"id":1,"title": "bugia", "content": "lol"}},{"foo":103, "art":{"id":1,"title": null}}]'
```

It will print out an ID. You can view the status of that `WfRun` via:

```
lhctl get wfRun <the id from the previous step>
```
