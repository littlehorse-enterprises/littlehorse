## Running WorkerContext Example

This example shows how to get access to the context when executing a task.
See the task implementation in `src/index.ts`.

Let's run the example in `src/index.ts`

```
npm install
npm start
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-worker-context request-time $(date +%s%3N)
```

> `date +%s%3N` is a gnu command https://www.gnu.org/software/coreutils/manual/html_node/date-invocation.html

Or:

```
npm run run-wf
npm run run-wf -- 1730000000000
```

In addition, you can check the result with:

```
# This call shows the result
lhctl get wfRun <wf_run_id>

# This will show you all nodes in the run
lhctl list nodeRun <wf_run_id>

# This shows the task run information
lhctl get taskRun <wf_run_id> <task_run_global_id>
```

## Considerations

If you need access to the context, add a `WorkerContext` parameter to your task function. It should be the last parameter:

```typescript
async function task(requestTime: number, ctx: WorkerContext): Promise<void> {
  // ...
}
```
