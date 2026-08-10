import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, VariableMutationType, WfRunId } from 'littlehorse-client/proto'

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
  const sink = createTaskWorker(() => 'ok', 'mutation-sink', config, { inputVars: {} })
  await ensureTaskDef(sink)
  await sink.start()

  const wf = Workflow.newWorkflow('example-mutation', thread => {
    const balance = thread.declareInt('balance').withDefault(100)
    const label = thread.declareStr('label').withDefault('balance: ')

    thread.mutate(balance, VariableMutationType.ADD, 50)
    thread.mutate(balance, VariableMutationType.SUBTRACT, 25)
    thread.mutate(balance, VariableMutationType.MULTIPLY, 2)
    // String concatenation is the same mutation machinery.
    thread.mutate(label, VariableMutationType.ADD, 'done')

    thread.execute('mutation-sink')
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-mutation', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-mutation -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await sink.close()
}

main().catch(err => { console.error(err); process.exit(1) })
