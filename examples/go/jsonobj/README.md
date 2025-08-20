# JSON Object Example

This is a "hello-world" with a JSON Object example of LittleHorse using GoLang.

## What's Going On

The file [workflow.go](./workflow.go) has two functions defined:

- `GetInfo`, which is a Task Function (task accepts a struct as an input variable; only pointers to struct are supported).
- `MyWorkflowGet`, which is the Workflow Function.

## Deploy the Task Worker

Before we can create the `WfSpec`, we need to register the `TaskDef`. The easiest way to do that is to run the task worker:

```
go run ./examples/jsonobj/worker
```

Leave that process running.

## Register the `WfSpec`

Next, in another terminal, run:

```
go run ./examples/jsonobj/deploy
```

That will create the `WfSpec`. You can verify that via:

```
lhctl get wfSpec json-object-workflow
```

## Run a `WfRun`

To run a `WfRun`, you can use `lhctl`.

```
 lhctl run json-object-workflow input '{"foo": 1, "art":{"id":1,"title": "bugia", "content": "lol"}}'
```

It will print out an ID. You can view the status of that `WfRun` via:

```
lhctl get wfRun <the id from the previous step>
```
