# Using Spawn Threads for Each element in a JSON Array and Assign User tasks

In this example we add a parallel approval workflow where we use most of the capabilities of User tasks and
spawned threads

## Create workflow spec

```
python -m parallel_approvals
```
## Run the Workflow
```
lhctl run parallel-approvals approvals '[{"userId": "pedro", "userGroup":"finance"},{"userId": null, "userGroup":"it"}]'
```

## Search user tasks
```
lhctl search userTaskRun --userId pedro
lhctl search userTaskRun --userGroup it
```

## Execute
```
lhctl execute userTaskRun <wfRunId> <userTaskGuid>
```
