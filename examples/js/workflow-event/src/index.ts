import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
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
  const pack = createTaskWorker(() => 'ORDER-7', 'we-pack', config, { inputVars: {} })
  await ensureTaskDef(pack)
  await pack.start()

  await client.putWorkflowEventDef({
    name: 'example-order-shipped',
    contentType: { returnType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.STR }, masked: false } },
  })

  const wf = Workflow.newWorkflow('example-workflow-event', thread => {
    const orderId = thread.declareStr('orderId')
    orderId.assign(thread.execute('we-pack'))
    // Anything watching this WfRun can observe the event.
    thread.throwEvent('example-order-shipped', orderId)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-workflow-event', variables: {} })
  const finished = await waitForRun(client, run.id!)

  const events = await client.listWorkflowEvents({ wfRunId: run.id })
  console.log(`example-workflow-event -> ${LHStatus[finished.status]}, ${events.results.length} event(s) emitted`)

  await pack.close()
}

main().catch(err => { console.error(err); process.exit(1) })
