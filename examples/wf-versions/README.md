## Running WorkflowVersionsExample

In this example you will learn how to define several WfSpec versions, and
how to run them.

Let's run the example in `WorkflowVersionsExample.java`

```
gradle example-wf-versions:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-wf-versions --wfSpecVersion 0 input-name Obi-Wan
lhctl run example-wf-versions --wfSpecVersion 1 input-name Obi-Wan
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRunModel <wf_run_id>

# This will show you all nodes in tha run
lhctl list nodeRunModel <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Getting the WfSpec

You can see the latest version of your `WfSpec` as follows:

```
lhctl get wfSpecModel example-wf-versions
```

You can get a specific version as follows:

```
lhctl get wfSpecModel example-wf-versions --v 1

lhctl get wfSpecModel example-wf-versions --v 0
```

## Other considerations

Besides the class `WorkflowImpl` has some utility functions like `doesWfSpecExist` or `registerWfSpec`,
we also highly recommend to use the `LHClient` to perform action over the wf spec:

```
Properties props = getConfigProps();
LHWorkerConfig config = new LHWorkerConfig(props);
LHClient client = new LHClient(config);

WfSpecPb wfSpecModel = client.getWfSpec("example-wf-versions");
wfSpecModel.getName()
wfSpecModel.getVersion()
        ...
```
