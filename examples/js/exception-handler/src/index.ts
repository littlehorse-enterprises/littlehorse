import { LHConfig, LHTaskException, LHTaskWorker, WorkerContext, Workflow, createTaskWorker } from 'littlehorse-client'
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
  // Throwing LHTaskException raises a *business* EXCEPTION, which is a decision
  // the workflow author is expected to handle. A plain Error would instead be a
  // technical ERROR, which the server retries before giving up.
  const risky = createTaskWorker(() => {
    throw new LHTaskException('credit-declined', 'the customer was declined')
  }, 'exc-risky', config, { inputVars: {} })
  const compensate = createTaskWorker(() => 'compensated', 'exc-compensate', config, { inputVars: {} })
  const flaky = createTaskWorker((ctx: WorkerContext) => {
    if (ctx.getAttemptNumber() < 1) throw new Error('transient outage')
    return 'recovered'
  }, 'exc-flaky', config, { inputVars: {} })
  for (const w of [risky, compensate, flaky]) { await ensureTaskDef(w); await w.start() }

  const wf = Workflow.newWorkflow('example-exception-handler', thread => {
    const node = thread.execute('exc-risky')

    // The handler runs instead of the failure propagating, so the WfRun still
    // completes. Without it, this run would end in EXCEPTION.
    thread.handleException(node, 'credit-declined', handler => {
      handler.execute('exc-compensate')
    })

    // A technical ERROR is retried first; handleError catches it only if every
    // attempt is exhausted. This one succeeds on its retry.
    thread.execute('exc-flaky').withRetries(2)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-exception-handler', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-exception-handler -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await Promise.all([risky.close(), compensate.close(), flaky.close()])
}

main().catch(err => { console.error(err); process.exit(1) })
