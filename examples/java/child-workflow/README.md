# Child Workflows

This is an example of a three-level workflow hierarchy with `parent`, `child`, and `grand-child` `WfSpec`s, demonstrating nested workflow relationships. The `child` `WfSpec` uses a `Variable` defined from the `parent`, and the `grand-child` `WfSpec` inherits from the `child`. This example shows the ability to use the `WfRunVariableAccessLevel` enum to inherit variables across multiple levels of workflow hierarchy.

In this example, there is a variable called `name` which is defined in the `parent` `WfSpec`. The `child` `WfSpec` refers to the parent `name` variable, and does the following:
* Passes it as a parameter into the `greet` task.
* At the end of the workflow, mutates it and sets it to `"yoda"`.

The `grand-child` `WfSpec` extends this hierarchy further by:
* Inheriting from the `child` workflow
* Waiting for an external event (`some-event`) to complete its execution

## Run the Application

First, run the application. This creates all three `WfSpec`s (`parent`, `child`, and `grand-child`) and registers a Task Worker.
```
./gradlew example-child-workflow:run
```

In another terminal, use `lhctl` to run the parent workflow:

```
-> lhctl run parent name obi-wan --wfRunId my-parent-wf
...
-> lhctl get variable my-parent-wf 0 name
```

As you can see, the value of the variable is `obi-wan`

Next, run the child:

```
lhctl run child --parentWfRunId my-parent-wf --wfRunId my-child-wf
```

You can use `lhctl get wfRun` to inspect the parent's result:

```
-> lhctl get wfRun my-parent-wf_my-child-wf
...
-> lhctl get variable my-parent-wf 0 name
```

You can see that the parent variable was set to `yoda`!

### Running the Grand-Child Workflow

You can also run the grand-child workflow, which inherits from the child workflow:

```
lhctl run grand-child --parentWfRunId my-child-wf --wfRunId my-grand-child-wf
```

The grand-child workflow will wait for an external event. To complete it, send the expected event:

```
lhctl put external-event my-grand-child-wf some-event '{"message": "Hello from grand-child!"}'
```

You can monitor the grand-child workflow status:

```
lhctl get wfRun my-grand-child-wf
```
