# StructBuilder [Experimental]

This example demonstrates building `Struct` values inside a WfSpec using the
`buildStruct` and `buildInlineStruct` APIs.

Rather than relying on a task to return a fully-formed `Struct`, the
`assemble-person` workflow assembles a `person` struct by combining input
variables (`name`, `email`) with the output of a `fetch-address` task, all
within the workflow definition itself.

## Key Concepts

- `thread.buildStruct('person')` creates a builder tied to the `person`
  StructDef.
- `.put('fieldName', value)` sets each field using workflow variables, task
  outputs, or literals.
- `thread.buildInlineStruct()` creates an inline builder for nested
  sub-structures (like `address` inside `person`); only the declared type of
  the variable has to be a registered StructDef.

## Workflow Overview

```
Input: name (STR), email (STR)
  |
  v
[fetch-address] returns an Address struct by name
  |
  v
Build "person" struct using buildStruct:
  - name  <- input variable
  - email <- input variable
  - address <- buildInlineStruct from fetch-address output fields
  |
  v
[save-person] receives the assembled Person struct
```

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

> Note: like the Java examples it mirrors, this example and `struct-def` both
> register StructDefs named `person` and `address` with different shapes, so
> they cannot share one tenant. Run them against separate tenants or servers.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the workers. This registers the `address` and `person` StructDefs, the
`fetch-address` and `save-person` TaskDefs, and the `assemble-person` WfSpec,
then keeps polling:

```bash
npm install
npm start
```

In another terminal, run the workflow:

```bash
lhctl run assemble-person name Obi-Wan email obi-wan@jedi.org
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
