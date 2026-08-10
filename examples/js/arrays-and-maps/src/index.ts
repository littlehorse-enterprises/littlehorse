import { LHConfig, LHTaskWorker, Workflow, arrayOf, createTaskWorker, mapOf } from 'littlehorse-client'
import { LHStatus, VariableType, WfRunId } from 'littlehorse-client/proto'

const config = LHConfig.from({})
const client = config.getClient()

/** Polls until the WfRun reaches a terminal status, then returns it. */
async function waitForRun(client: ReturnType<LHConfig['getClient']>, id: WfRunId) {
  for (let i = 0; i < 150; i++) {
    const run = await client.getWfRun(id)
    if (run.status === LHStatus.COMPLETED || run.status === LHStatus.ERROR || run.status === LHStatus.EXCEPTION) {
      return run
    }
    await new Promise(r => setTimeout(r, 200))
  }
  throw new Error('WfRun did not finish in time')
}

/** Registers the TaskDef behind a worker if the server does not have it yet. */
async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const sink = createTaskWorker(() => 'ok', 'collections-sink', config, { inputVars: {} })
  await ensureTaskDef(sink)
  await sink.start()

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

  // Typed collections are supplied as real LIST / MAP values. Assigning a plain
  // JS literal inside the workflow would compile to JSON_ARR / JSON_OBJ, and the
  // server rejects that with "Cannot assign JSON_ARR to null without explicit
  // casting" — a typed ARRAY and a JSON_ARR are genuinely different types here,
  // not two spellings of the same thing.
  const run = await client.runWf({
    wfSpecName: 'example-arrays-and-maps',
    variables: {
      tags: {
        value: {
          oneofKind: 'array',
          array: {
            items: [
              { value: { oneofKind: 'str', str: 'alpha' } },
              { value: { oneofKind: 'str', str: 'beta' } },
            ],
          },
        },
      },
      scores: {
        value: {
          oneofKind: 'map',
          map: {
            entries: [
              { key: { value: { oneofKind: 'str', str: 'alice' } }, value: { value: { oneofKind: 'int', int: '10' } } },
              { key: { value: { oneofKind: 'str', str: 'bob' } }, value: { value: { oneofKind: 'int', int: '20' } } },
            ],
          },
        },
      },
    },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-arrays-and-maps -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await sink.close()
}

main().catch(err => { console.error(err); process.exit(1) })
