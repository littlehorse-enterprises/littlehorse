## Running SpawnParallelThreadsFromJsonArrVariableExample

In this example you will see how to spawn multiples threads base on a INPUT json array.

Let's run the example in `SpawnThreadForEachExample`

```
dotnet build
dotnet run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run spawn-parallel-threads-from-json-arr-variable approval-chain '{"description": "demo for approvals", "approvals":  [{"user": "yoda"}, {"user": "chewbacca"}, {"user": "anakin"}]}'
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# See the input variables to each ThreadRun
lhctl get variable <wf_run_id> 1 INPUT  # yoda
lhctl get variable <wf_run_id> 2 INPUT  # chewbacca
lhctl get variable <wf_run_id> 3 INPUT  # anakin

lhctl get variable <wf_run_id> 1 not-used-variable  # 1234
lhctl get variable <wf_run_id> 2 not-used-variable  # 1234
lhctl get variable <wf_run_id> 3 not-used-variable  # 1234
```
