# Littlehorse gRPC Client

This is the generated library to perform gRPC calls to littlehorse server from nodejs.
For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io/docs/server).

## Installation

```bash
npm install littlehorse-client
```

## Usage

### With Properties File

```ini
# File located at /example/littlehorse.config

LHC_API_HOST=local.littlehorse.cloud
LHC_API_PORT=2023
LHC_API_PROTOCOL=TLS
LHC_TENANT_ID=example
```

```ts
import { LHConfig } from 'littlehorse-client'

const config = new LHConfig.fromPropertiesFile('/example/littlehorse.config')

const client = config.getClient({
  // accessToken: "..." // optionally provide an OAuth token
})

// client exposes all gRPC methods
await client.whoami({})
await client.getTaskDef({ name: 'sample-task' })
await client.getWfRun({ id: '1674938f8aca437e963083020fa19182' })
```

### From config object

```ts
import { LHConfig } from 'littlehorse-client'

const config = new LHConfig.from({
  apiHost: 'local.littlehorse.cloud',
  apiPort: '2023',
  protocol: 'PLAINTEXT',
  tenantId: 'example',
})

const client = config.getClient({
  // accessToken: "..." // optionally provide an OAuth token
})

await client.whoami({})
await client.getTaskDef({ name: 'sample-task' })
await client.getWfRun({ id: '1674938f8aca437e963083020fa19182' })
```
