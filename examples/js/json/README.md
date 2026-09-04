# Json

Serialize and deserialize JSON so task functions work with real objects.
The workflow reaches into a JSON variable with json-path; more information
about json-path at https://github.com/json-path/JsonPath.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDefs and the WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run example-json person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
