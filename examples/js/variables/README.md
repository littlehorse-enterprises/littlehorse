# Variables

A workflow that involves several types of variables: STR, INT (64-bit
integer), DOUBLE (floating-point number), BOOL (boolean), and JSON_OBJ (JSON
object). The steps are:

1. Pass the `input-text` variable into the `sentiment-analysis` task, which
   simulates a sentiment score with a random double between 0.0 and 100.0.
2. The `process-text` task takes the score plus the other inputs and builds a
   JSON object out of them.
3. The `send` task prints the resulting JSON object.

The variables are `searchable`, and the sensitive ones are `masked` so they
are hidden in the dashboard and in search results.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the workers. They register the TaskDefs and the WfSpec, then keep
polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run example-variables input-text 'this is a very long text' add-length false user-id 1234
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

Search by variable value, for example by `user-id`:

```bash
lhctl search variable --name user-id --value 1234 --varType INT --wfSpecName example-variables
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
