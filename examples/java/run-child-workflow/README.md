# Run Child Workflow Node

This example shows how a `WfSpec` can invoke another `WfSpec` as a child using the `WorkflowThread#runWf()` capability.

## Run the Application

First, run the application. This creates both `WfSpec`s (`my-parent` and `some-other-wfspec`) and registers a Task Worker for a few `TaskDef`s.

```
./gradlew example-run-child-workflow:run
```

To see that `some-other-wfspec` really isn't special, you can run it on its own (note it is just like the [basic example](../basic/)):

```
-> lhctl run some-other-wfspec child-input-name colt
```

In another terminal, use `lhctl` to run the parent workflow:

```
-> lhctl run my-parent input-name colt
{
  "id": {
    "id": "9b90f870e8044fa7b47ae4b3b2b6ab9b"
  },
  "wfSpecId": {
    "name": "my-parent",
    "majorVersion": 0,
    "revision": 0
  },
  // ...
}
```

You can see the result of the `RunChildWfNode` by looking at the `NodeRun`:

```
-> lhctl get nodeRun 9b90f870e8044fa7b47ae4b3b2b6ab9b 0 1
{
  "id": {
    "wfRunId": {
      "id": "9b90f870e8044fa7b47ae4b3b2b6ab9b"
    },
    "threadRunNumber": 0,
    "position": 1
  },
  // ...
  "runChildWf": {
    "childWfRunId": {
      "id": "fea26bfe03474ef3b8d487cffe5f1a3d",
      "parentWfRunId": {
        "id": "9b90f870e8044fa7b47ae4b3b2b6ab9b"
      }
    }
  }
}
```

You can fetch the resulting child `WfRun` as follows (`lhctl get wfRun {parent-wf-run-id}_{child-wf-run-id}`):

```
lhctl search wfRun byParent 9b90f870e8044fa7b47ae4b3b2b6ab9b
```

```
lhctl get wfRun 9b90f870e8044fa7b47ae4b3b2b6ab9b_fea26bfe03474ef3b8d487cffe5f1a3d
```

## Compare to Hierarchical Workflows

In the [Hierarchical Workflow](../hierarchical-workflow/) example, we also have `WfRun`'s that have a parent and child relationship. The difference is that:

* In the hierarchical workflow example, the `child` `WfSpec` _depends on_ being the child of the parent `WfSpec`: in fact, it even accesses variables from the parent.
* In this example (`RunChildWfNode`), the child `WfSpec` has no knowledge of the parent: any random `WfSpec` can be run from the `RunChildWfNode`. This means that the child `WfRun` cannot access anything from the parent.

