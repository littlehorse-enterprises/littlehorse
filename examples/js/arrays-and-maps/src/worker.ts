import { LHTaskWorker, Workflow, arrayOf, createTaskWorker, mapOf } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()

function acknowledge(): string {
  return 'ok'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const sink = createTaskWorker(acknowledge, 'collections-sink', config, { inputVars: {} })
  await ensureTaskDef(sink)

  const wf = Workflow.newWorkflow('example-arrays-and-maps', thread => {
    // Native typed collections, not JSON blobs — the server knows the element
    // and key/value types, so they are type-checked rather than opaque.
    thread.declareArray('tags', VariableType.STR).required()
    thread.declareMap('scores', VariableType.STR, VariableType.INT).required()

    // arrayOf / mapOf nest, so collections of collections are expressible.
    thread.declareArray('matrix', arrayOf(VariableType.INT))
    thread.declareMap('lookup', VariableType.STR, mapOf(VariableType.STR, VariableType.STR))

    thread.execute('collections-sink')
  })
  await wf.registerWfSpec(config)

  await sink.start()
  console.log('ready: polling for collections-sink tasks')
  console.log(
    'run the workflow:  lhctl run example-arrays-and-maps tags \'["alpha","beta"]\' scores \'{"alice":10,"bob":20}\''
  )

  const shutdown = async () => {
    await sink.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
