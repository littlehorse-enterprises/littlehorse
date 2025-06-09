## Running DataNugget Example

```
./gradlew example-data-nugget:run
```

Look at the `ExternalEvent`:

```
lhctl get externalEventDef document-signed
```

Now create a `DataNugget`. The `--guid` flag is optional if you want to create a `DataNugget`. If you want to update it, it is necessary.

```
lhctl put dataNugget document-signed asdf BOOL true --guid 5c4c77d4c5ce4e05a4a4b1995bf3478c
```

Get the nugget!

```
lhctl get dataNugget document-signed asdf 5c4c77d4c5ce4e05a4a4b1995bf3478c
```

Update it:

```
lhctl put dataNugget document-signed asdf BOOL false --guid 5c4c77d4c5ce4e05a4a4b1995bf3478c
```

Update it with an invalid epoch:

```
lhctl put dataNugget document-signed asdf BOOL false --guid 5c4c77d4c5ce4e05a4a4b1995bf3478c --epoch 5
```
