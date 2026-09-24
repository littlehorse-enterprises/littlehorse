# StructDef [Experimental]

`StructDef`s let you define schemas for the data in your workflows. This
example registers three of them with `lhStruct(...)` zod schemas: `address`,
`person`, and `parking-ticket-report`. In `person`, the `homeAddress` field is
masked and nullable: task workers still see the full address so they can mail
the ticket, but public API responses do not expose that sensitive PII, and the
address may legitimately be unknown.

The `issue-parking-ticket` workflow takes a `ticket-report` Struct as input,
executes `get-car-owner` (which returns a `person` Struct), and passes the
owner to `mail-ticket`.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code, no checked-in JSON, no `lhctl deploy`.

## Prerequisites

A running LittleHorse server; see [`../../README.md`](../../README.md) for the
one-command setup. Examples read `~/.config/littlehorse.config` when it
exists, else they connect to `localhost:2023`.

## Run it

Start the worker. It registers the StructDefs, the TaskDefs, and the WfSpec,
then keeps polling:

```bash
npm install
npm start
```

Verify that the `person` StructDef was created on the server (this works for
any of the three, using the name passed to `lhStruct`):

```bash
lhctl get structdef person 0
```

In another terminal, run the workflow, passing the `ParkingTicketReport`
Struct as a JSON input variable. `lhctl` looks up the registered `StructDef`
for the `ticket-report` variable and coerces each JSON field against its
declared type:

```bash
lhctl run issue-parking-ticket ticket-report '{"vehicleMake": "BARC", "vehicleModel": "Speeder", "licensePlateNumber": "1HGCM82633A004352"}'
```

To showcase nullable Struct fields, use a plate that starts with `NOADDR`; in
this branch the worker returns a `person` with `homeAddress` set to null:

```bash
lhctl run issue-parking-ticket ticket-report '{"vehicleMake": "Starfighter", "vehicleModel": "Naboo", "licensePlateNumber": "NOADDR-42"}'
```

Check the result:

```bash
lhctl get wfRun <wf_run_id>
```

No `lhctl`? `npm run trigger` starts one run and prints the result.
