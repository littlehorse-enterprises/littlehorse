import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { Comparator, LHStatus, WfRunId } from 'littlehorse-client/proto'

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
  const proceed = createTaskWorker(() => 'threshold reached', 'wfc-proceed', config, { inputVars: {} })
  await ensureTaskDef(proceed)
  await proceed.start()

  await client.putExternalEventDef({ name: 'example-stock-updated', contentType: {} })

  const wf = Workflow.newWorkflow('example-wait-for-condition', thread => {
    const stock = thread.declareInt('stock').withDefault(0)
    // The run parks here until the condition holds; the event below moves it.
    stock.assign(thread.waitForEvent('example-stock-updated'))
    thread.waitForCondition(thread.condition(stock, Comparator.GREATER_THAN_EQ, 10))
    thread.execute('wfc-proceed')
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-wait-for-condition', variables: {} })
  await client.putExternalEvent({
    wfRunId: run.id,
    externalEventDefId: { name: 'example-stock-updated' },
    content: { value: { oneofKind: 'int', int: '25' } },
  })

  const finished = await waitForRun(client, run.id!)
  console.log(`example-wait-for-condition -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await proceed.close()
}

main().catch(err => { console.error(err); process.exit(1) })
