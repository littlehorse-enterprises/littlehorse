import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'

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
  const sink = createTaskWorker(() => 'ok', 'variables-sink', config, { inputVars: {} })
  await ensureTaskDef(sink)
  await sink.start()

  const wf = Workflow.newWorkflow('example-variables', thread => {
    // One of each primitive the SDK can declare.
    thread.declareStr('a-string').withDefault('hello').searchable()
    thread.declareInt('an-int').withDefault(42)
    thread.declareDouble('a-double').withDefault(3.14)
    thread.declareBool('a-bool').withDefault(true)
    thread.declareTimestamp('a-timestamp')
    thread.declareJsonObj('a-json-obj').withDefault({ nested: { ok: true } })
    thread.declareJsonArr('a-json-arr').withDefault([1, 2, 3])
    // Masked variables are hidden in the dashboard and in search results.
    thread.declareStr('a-secret').withDefault('hunter2').masked()
    thread.execute('variables-sink')
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-variables', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-variables -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await sink.close()
}

main().catch(err => { console.error(err); process.exit(1) })
