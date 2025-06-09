## Running CorrelatedEvent Example

This example has a simple one-step `WfSpec` which just waits for an external event with a correlation ID. To run it (and run a WfRun):

```
./gradlew example-correlated-event:run
```

Look at the `ExternalEventDef`:

```
lhctl get externalEventDef document-signed
```

Run a `WfRun`:

```
lhctl run correlated-events document-id my-document-id-asdf
```

Now complete the `WfRun` by posting a `CorrelatedEvent`:

```
lhctl put correlatedEvent my-document-id-asdf document-signed BOOL true
```

Note that if you repeat the above command, you get `ALREADY_EXISTS`!
