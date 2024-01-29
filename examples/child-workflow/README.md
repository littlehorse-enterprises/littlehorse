# Child Workflows

This is a basic example of two `WfSpec`s, `parent` and `child`, in which the `child` `WfSpec` uses a `Variable` defined from the `parent`. It demonstrates the ability to use the `WfRunVariableAccessLevel` enum to inherit variables from the parent, and also modify them.

In this example, there is a variable called `name` which is defined in the `parent` `WfSpec`. The `child` `WfSpec` refers to the parent `name` variable, and does the following:
* Passes it as a parameter into the `greet` task.
* At the end of the workflow, mutates it and sets it to `"yoda"`.

## Run the Application

First, run the application. This creates both `WfSpec`'s and registers a Task Worker.
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
