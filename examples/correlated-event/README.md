## Running CorrelatedEvent Example

```
./gradlew example-data-nugget:run
```

Look at the `ExternalEvent`:

```
lhctl get externalEventDef document-signed
```

Now create a `CorrelatedEvent`.

```
lhctl put correlatedEvent document-signed asdf BOOL true
```

Get the correlated event!

```
lhctl get correlatedEvent asdf document-signed
```
