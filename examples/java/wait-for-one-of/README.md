## Wait for OneOf

This example shows how `waitForOneOf()` works: you can wait for multiple Child `ThreadRun`s and continue once any of them is done.

Let's run the example in `WaitForOneOfExample.java`

```
./gradlew example-wait-for-one-of:run
```

In another terminal, use `lhctl` to run the workflow:

```
lhctl run example-wait-for-one-of --wfRunId my-wf-run
```

Now you can choose whether to complete child thread 1 or child thread 2:

```
lhctl postEvent my-wf-run child-1-event STR "hello"
```

Or

```
lhctl postEvent my-wf-run child-2-event STR "hello"
```
