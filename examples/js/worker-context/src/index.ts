import { LHConfig, LHTaskWorker, WorkerContext, Workflow, createTaskWorker } from 'littlehorse-client'
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

let charges = 0

async function main() {
  const charge = createTaskWorker(
    async (ctx: WorkerContext) => {
      ctx.log(`attempt ${ctx.getAttemptNumber()} for wfRun ${ctx.getWfRunId()?.id}`)
      // executeAndCheckpoint runs at most once across retries — the server
      // remembers the result, so the side effect is not repeated.
      const receipt = await ctx.executeAndCheckpoint(() => {
        charges++
        return `receipt-${charges}`
      })
      if (ctx.getAttemptNumber() < 1) throw new Error('failing once, after the checkpoint')
      return receipt
    },
    'wc-charge',
    config,
    { inputVars: {} }
  )
  await ensureTaskDef(charge)
  await charge.start()

  const wf = Workflow.newWorkflow('example-worker-context', thread => {
    const receipt = thread.declareStr('receipt')
    receipt.assign(thread.execute('wc-charge').withRetries(2))
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-worker-context', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-worker-context -> ${LHStatus[finished.status]}, side effects: ${charges} (expected 1)`)

  await charge.close()
}

main().catch(err => { console.error(err); process.exit(1) })
