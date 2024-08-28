# If Conditional

This is a simple example of a workflow with if/else functionality in LittleHorse. The pseudocode is as follows:

```
int number_of_donuts = input()

if number_of_donuts > 10:
    eat_salad()
else:
    eat_donut()
```

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/conditionals/ifelse/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/conditionals/ifelse/deploy
```

## Run a `WfRun`

Let's run the `WfRun` with a small amount of donuts:

```
lhctl run donut-workflow number-of-donuts 3
```

Let's find and copy the TaskGuid
```
lhctl get nodeRun <wfRunId from before> 0 2
```

Let's view the output of the Task:

```
lhctl get taskRun <wfRunId from before> <taskGuid from before> | jq '.attempts[0].output.str'
```

That should print:

```
Have another donut!
```

Let's run it after we've had too many donuts:

```
lhctl run donut-workflow number-of-donuts 15
```

Let's find and copy the TaskGuid
```
lhctl get nodeRun <wfRunId from before> 0 2
```

Let's view the output of the Task:

```
lhctl get taskRun <wfRunId from before> <taskGuid from before> | jq '.attempts[0].output.str'
```

That should print:

```
Have a salad!
```
