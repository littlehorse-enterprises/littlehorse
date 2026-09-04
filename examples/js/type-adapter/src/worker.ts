import { LHTaskWorker, WorkerContext, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'
import { UUID, UUIDTypeAdapter } from './uuid-adapter'

const config = loadConfig()
// The adapter maps UUID -> STR whenever the worker serializes a value, so
// task functions can return UUID instances directly.
config.addTypeAdapter(new UUIDTypeAdapter())

function getUUID(): UUID {
  const uuid = UUID.randomUUID()
  console.log(`Generated UUID ${uuid}`)
  return uuid
}

function echoUUID(uuidStr: string, context: WorkerContext): void {
  // Unlike Java, sdk-js applies adapters only when serializing: task input
  // arrives as the transported STR, so the STR -> UUID mapping is explicit.
  const uuid = UUID.fromString(uuidStr)
  console.log(`Received UUID ${uuid}`)
  context.log(`Received UUID via adapter: ${uuid}`)
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const getUuidWorker = createTaskWorker(getUUID, 'get-uuid', config, {
    inputVars: {},
    outputSchema: z.string(),
    description: 'Generates and returns a random UUID.',
  })
  const echoUuidWorker = createTaskWorker(echoUUID, 'echo-uuid', config, {
    inputVars: { uuid: z.string() },
    description: 'Receives a UUID and writes it to task log output.',
  })

  await ensureTaskDef(getUuidWorker)
  await ensureTaskDef(echoUuidWorker)

  const wf = Workflow.newWorkflow('example-type-adapter', thread => {
    const uuidVar = thread.declareStr('uuid').searchable()
    uuidVar.assign(thread.execute('get-uuid'))
    thread.execute('echo-uuid', uuidVar)
  })
  await wf.registerWfSpec(config)

  await getUuidWorker.start()
  await echoUuidWorker.start()
  console.log('ready: polling for get-uuid and echo-uuid tasks')
  console.log('run the workflow:  lhctl run example-type-adapter')

  const shutdown = async () => {
    await getUuidWorker.close()
    await echoUuidWorker.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
