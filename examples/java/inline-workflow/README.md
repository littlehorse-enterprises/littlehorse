# Inline workflows with lhctl

This example registers three TaskDefs, a StructDef, and starts their Java workers. It does not
define or register a WfSpec. Instead, `lhctl execute task` builds an inline
workflow containing a task invocation and starts a WfRun immediately.

## Start the workers

Use Java 25 and a running LittleHorse server that supports inline workflows.
Build `lhctl` from this checkout to include the `execute task` command:

```bash
# From the repository root
(cd lhctl && go install .)
lhctl whoami
./gradlew example-inline-workflow:run
```

The workers load `~/.config/littlehorse.config`, when present, and support the
usual `LHC_*` environment variables. Configure `lhctl` to use the same server
and tenant. Keep the workers running and execute the following commands in
another terminal.

## Execute tasks

The registered tasks are:

| TaskDef | Inputs (in order) | Output |
| --- | --- | --- |
| `inline-greet` | `name`: STR, `age`: INT | A greeting string |
| `inline-add` | `left`: INT, `right`: INT | The integer sum |
| `inline-greet-person` | `person`: STRUCT (`inline-person`) | A greeting string |

Pass positional values in TaskDef input order. `lhctl` resolves their types
from the registered TaskDef:

```bash
lhctl execute task inline-greet Ada 2
lhctl execute task inline-add 10 20
```

Alternatively, use repeated `--arg name:value` flags in any order:

```bash
lhctl execute task inline-greet --arg age:2 --arg name:Ada
lhctl execute task inline-add --arg right:20 --arg left:10
```

Do not mix positional values and `--arg` flags. Quote values containing spaces,
for example `--arg 'name:Ada Lovelace'`. The Gradle build preserves Java parameter
names so the TaskDefs expose `name`, `age`, `left`, `right`, and `person`.

Each command creates a separate inline WfRun with literal task arguments and
prints the WfRun immediately; it does not wait for the worker to finish. The
task's return value becomes the inline workflow's output: the greeting is
`Hello, Ada! You are 2 years old.`, and the sum is `30`.

## Execute a task with a struct input

`inline-greet-person` receives a Java `Person` record annotated with
`@LHStructDef("inline-person")`. The example registers that StructDef before
registering the task. Its fields are `name` (STR) and `age` (INT).

Pass the struct as a JSON object, either positionally or as a named argument:

```bash
lhctl execute task inline-greet-person '{"name":"Ada Lovelace","age":36}'
lhctl execute task inline-greet-person --arg 'person:{"name":"Ada Lovelace","age":36}'
```

Single quotes keep the JSON intact as one shell argument. `lhctl` looks up the
TaskDef and its StructDef to convert the JSON into a typed struct literal; no
protobuf wrapper or StructDef ID is needed in the input. The Java worker receives
a `Person`, and the workflow output is `Hello, Ada Lovelace! You are 36 years old.`
As with the other tasks, each command starts a separate inline workflow without
registering a WfSpec.

## Inspect a run

Use the ID printed by the command, or choose one explicitly:

```bash
lhctl execute task inline-add 10 20 --wfRunId inline-add-example
lhctl get wfRun inline-add-example
lhctl list nodeRun inline-add-example
```

After completion, the WfRun has status `COMPLETED` and its entrypoint thread has
the task's output. It references an InlineWfSpec rather than a registered WfSpec.
Use a different `--wfRunId` for subsequent runs, or omit it to generate an ID.

Stop the workers with Ctrl+C when finished.
