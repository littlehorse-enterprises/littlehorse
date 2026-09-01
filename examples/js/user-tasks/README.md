# User Tasks

Assign a UserTask and complete it as a human would.

Built with the `sdk-js` **wfsdk**: the `WfSpec` is defined in TypeScript and
registered from code — no checked-in JSON, no `lhctl deploy`.

## Run it

Start a server if you do not have one:

```bash
docker run --rm -d -p 2023:2023 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master
```

Then:

```bash
npm install
npm start
```

The example registers its metadata, starts its worker(s), launches one `WfRun`,
waits for it to finish, and prints the result.
