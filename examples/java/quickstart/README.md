## Running QuickstartExample

This example models a simple know-your-customer flow:

1. Run `verify-identity`.
2. Wait for a correlated `identity-verified` event.
3. Notify the customer whether they were verified.

Start the example from the repository root:

```bash
./gradlew quickstart:run
```

That single command registers the `TaskDef`s, `ExternalEventDef`, and `WfSpec`, then starts the task workers.

In another terminal, run the workflow:

```bash
lhctl run quickstart full-name 'Obi-Wan Kenobi' email obiwan@jedi.temple ssn 123456789
```

Then complete the waiting event with a correlated event:

```bash
lhctl put correlatedEvent obiwan@jedi.temple identity-verified BOOL true
```

Try `BOOL false` to follow the rejection branch instead.
