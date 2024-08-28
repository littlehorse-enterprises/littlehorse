# While Conditional

This is a simple example of a workflow with While functionality in LittleHorse. The pseudocode is as follows:

```
int number_of_donuts = input()

while number_of_donuts > 0:
    number_of_donuts = number_of_donuts - 1
```

## Start the Task Worker

We have two `TaskDef`'s and thus two Task Functions. Note that `worker/main.go` kicks off two threads, one for each Task Worker.

```
go run ./examples/conditionals/while/worker
```

## Register the `WfSpec`

In another terminal, run:

```
go run ./examples/conditionals/while/deploy
```

## Run a `WfRun`

Let's run the `WfRun` with a small amount of donuts:

```
lhctl run donut-workflow number-of-donuts 3
```

Let's look at the different outputs (donuts left) of each loop in the while

```
lhctl get nodeRun <wfRunId from before> 0 2
lhctl get taskRun <wfRunId from before> <taskGuid from before> | jq '.attempts[0].output.int'
```

That should print:

```
"2"
```

```
lhctl get nodeRun <wfRunId from before> 0 5
lhctl get taskRun <wfRunId from before> <taskGuid from before> | jq '.attempts[0].output.int'
```

That should print:

```
"1"
```

```
lhctl get nodeRun <wfRunId from before> 0 8
lhctl get taskRun <wfRunId from before> <taskGuid from before> | jq '.attempts[0].output.int'
```

That should print:

```
"0"
```
