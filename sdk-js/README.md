# LittleHorse SDK for JavaScript

The official LittleHorse SDK for Node.js and TypeScript. It contains the
gRPC client, the workflow builder that compiles TypeScript into WfSpecs,
task workers, and user task helpers. Full documentation lives at
[littlehorse.io](https://littlehorse.io/docs/server), and the runnable
examples live in this repo under `examples/js/`.

## Installation

```bash
npm install littlehorse-client
```

Task input schemas are declared with [zod](https://zod.dev), which is a
peer dependency. npm and pnpm install it automatically.

## Quickstart

Run a workflow end to end: define a task, register everything, run it,
and read the result.

```ts
import { LHConfig, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus } from 'littlehorse-client/proto'
import { z } from 'zod'

const config = LHConfig.from({})
const client = config.getClient()

const greeter = createTaskWorker((name: string) => `Hello, ${name}!`, 'greet', config, {
  inputVars: { name: z.string() },
})
await greeter.registerTaskDef()
await greeter.start()

const wf = Workflow.newWorkflow('quickstart', thread => {
  const name = thread.declareStr('name').required()
  const greeting = thread.declareStr('greeting')
  greeting.assign(thread.execute('greet', name))
})
await wf.registerWfSpec(config)

const run = await client.runWf({
  wfSpecName: 'quickstart',
  variables: { name: { value: { oneofKind: 'str', str: 'LittleHorse' } } },
})
```

Poll `client.getWfRun(run.id!)` until the status is `LHStatus.COMPLETED`,
read variables back with `client.getVariable(...)`, and call
`greeter.close()` when done, or the worker's poll streams keep the process
alive. On an OAuth-protected cluster, acquire the client with
`await config.getAuthenticatedClient()` instead of `getClient()`. The
`basic` example under `examples/js/` shows the complete version of this
program.

## Optional settings

Optional settings take an options object. The Java-style chained methods
also work, and both styles compile to the same WfSpec:

```ts
thread.declareStr('email', { required: true, masked: true })
thread.execute('charge', [card], { retries: 3, timeoutSeconds: 30 })
thread.waitForEvent('paid', { timeoutSeconds: 300, payloadSchema: z.boolean() })
```

## Configuration

`LHConfig` resolves settings from three sources. With `LHConfig.from()`,
`LHC_*` environment variables are read as the base layer and explicit args
override them; `fromConfigFile()` reads only the file plus built-in
defaults.

From the environment alone:

```ts
import { LHConfig } from 'littlehorse-client'

const config = LHConfig.from({})
```

From a properties file:

```ini
# /example/littlehorse.config
LHC_API_HOST=local.littlehorse.cloud
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHC_TENANT_ID=example
```

```ts
const config = LHConfig.fromConfigFile('/example/littlehorse.config')
```

From a config object:

```ts
const config = LHConfig.from({
  apiHost: 'local.littlehorse.cloud',
  apiPort: '2023',
  protocol: 'TLS',
  tenantId: 'example',
})
```

The client exposes every gRPC method:

```ts
const client = config.getClient() // or config.getClient('bearer-token')

await client.whoami({})
await client.getTaskDef({ name: 'sample-task' })
```

With OAuth configured, `await config.getAuthenticatedClient()` returns a
client that carries a freshly minted token.

## Development

Develop, build, and test the SDK with Node.js 24 and
[pnpm](https://pnpm.io/) 10.19.0. From `sdk-js/`, select the runtime declared
in `.nvmrc` and use the package manager declared in `package.json`:

```bash
nvm install
nvm use
corepack pnpm install --frozen-lockfile
corepack pnpm run build
corepack pnpm run test
```

The published SDK retains its `engines.node` range of `>=18.14.0` for
consumers. The Node 20 TypeScript compiler preset and TypeScript 5.3.3 are
retained to preserve the existing emitted JavaScript target; the compiler
preset does not select the development runtime. Node type definitions
already target Node 24.

Integration tests run against a real server in docker:

```bash
corepack pnpm run test:integration
```

The SDK is also a testee of the cross-SDK conformance suite; see
`sdk-conformance/README.md` at the repo root.
