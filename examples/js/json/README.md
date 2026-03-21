# JavaScript JSON variable example

[`examples/java/json`](../../../java/json/README.md) — `JSON_OBJ` + JsonPath into tasks.

## Prerequisites

- Node.js >= 18, server on `localhost:2023`
- `cd ../../../sdk-js && npm install && npm run build`

## Run

Terminal 1:

```bash
npm install
npm start
```

Terminal 2:

```bash
npm run run-wf
# or
npm run run-wf -- '{"name":"Obi-Wan","car":{"brand":"Ford","model":"Escape"}}'
```

Same idea as:

```bash
lhctl run example-json person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'
```
