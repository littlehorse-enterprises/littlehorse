# Timestamp

Declare a timestamp variable and use it across tasks.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

The workflow declares a `publish-date` TIMESTAMP variable with a default
value and a `book-name` STR variable, then runs three tasks:

- `publish-book` builds a book object holding the timestamp representations
  available in JS: a `Date`, an ISO-8601 string, a protobuf `Timestamp`,
  epoch milliseconds, and a local date-time string.
- `get-current-date` returns the current date as a TIMESTAMP.
- `print-book-details` receives the book and the current date, then logs the
  published book data and the current timestamp.

The zod schema mapping in `createTaskWorker` has no TIMESTAMP type yet, so
the worker registers these TaskDefs through the client with explicit type
definitions, matching the signatures the Java SDK derives from its worker
methods.

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
lhctl run example-timestamp book-name "My Book" publish-date 1997-06-26T12:12:12Z
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
