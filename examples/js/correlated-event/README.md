# Correlated Event

A simple one-step `WfSpec` which just waits for an external event with a
correlation ID. A `CorrelatedEvent` is posted with a key (here the
`document-id` variable) instead of a `WfRun` id, and the server matches it to
whichever runs are waiting on that key.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the registrar. It registers the ExternalEventDef and the WfSpec (this
example has no task workers), then stays up:

```bash
npm install
npm start
```

In another terminal, look at the `ExternalEventDef`:

```bash
lhctl get externalEventDef document-signed
```

Run a `WfRun`:

```bash
lhctl run correlated-events document-id my-document-id-asdf
```

Now complete the `WfRun` by posting a `CorrelatedEvent`:

```bash
lhctl put correlatedEvent my-document-id-asdf document-signed BOOL true
```

Note that if you repeat the above command, you get `ALREADY_EXISTS`!

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run, posts the `CorrelatedEvent`
(key `some-document`, or pass your own as the first argument) and prints the
result.
