# Expressions

A simple example, which does two things:

1. Declare the variables `quantity`, `price`, and `taxes`,
2. Execute an Expression to calculate the total to pay:
   `quantity * (price * (1 + (taxes / 100)))`.

The arithmetic is evaluated server-side; the `place-order` task receives the
calculated total.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the TaskDef and the WfSpec, then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
# Here, we specify that the "quantity" = 1, "price" = 0.8 and "taxes" = 12
lhctl run example-expressions quantity 1 price 0.8 taxes 12
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
