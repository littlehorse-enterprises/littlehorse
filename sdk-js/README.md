# Littlehorse gRPC Client

This is the generated library to perform gRPC calls to littlehorse server from nodejs.
For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev).

## Installation

```bash
npm install littlehorse-client
```

## Usage

### With Constructor

```ts
import { LHClient } from 'littlehorse-client'

// Configure the server
const config = {
  // host: "localhost:2023", // Hostname and port to littlehorse
  // ssl: {
  //   enabled: true,
  //   caRoot: "" // plain text CA cert
  // }
}
const client = new LHClient(config).getClient({
  // accessToken: "..." // optionally provide a OAuth token
  // tenantId: "..." // optionally provide a tenant id
})

// client exposes all gRPC methods
await client.whoami({})
await client.getTaskDef({ name: 'sample-task' })
await client.getWfRun({ id: '1674938f8aca437e963083020fa19182' })
```

### With factory method

```ts
import { createClient } from 'littlehorse-client'
const client = createClient({
  server: {
    // host: "localhost:2023", // Hostname and port to littlehorse
    // oauth: "" // optional true otherwise false
    // ssl: {
    //   enabled: true,
    //   caRoot: "" // plain text CA cert
    // }
  },
  client: {
    // accessToken: "..." // optionally provide a OAuth token
    // tenantId: "..." // optionally provide a tenant id
  },
})

await client.whoami({})
await client.getTaskDef({ name: 'sample-task' })
await client.getWfRun({ id: '1674938f8aca437e963083020fa19182' })
```
