import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { Comparator, LHStatus, WfRunId } from 'littlehorse-client/proto'
import { z } from 'zod'

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
  const describe = createTaskWorker((label: string) => `took the ${label} branch`, 'cond-describe', config, {
    inputVars: { label: z.string() },
  })
  await ensureTaskDef(describe)
  await describe.start()

  const wf = Workflow.newWorkflow('example-conditionals', thread => {
    const amount = thread.declareInt('amount').required()
    const outcome = thread.declareStr('outcome')

    thread
      .doIf(thread.condition(amount, Comparator.GREATER_THAN, 100), inner => {
        outcome.assign(inner.execute('cond-describe', 'large'))
      })
      .doElseIf(thread.condition(amount, Comparator.GREATER_THAN, 10), inner => {
        outcome.assign(inner.execute('cond-describe', 'medium'))
      })
      .doElse(inner => {
        outcome.assign(inner.execute('cond-describe', 'small'))
      })
  })
  await wf.registerWfSpec(config)

  // Run all three branches so the dashboard shows each path taken.
  for (const amount of [5, 50, 500]) {
    const run = await client.runWf({
      wfSpecName: 'example-conditionals',
      variables: { amount: { value: { oneofKind: 'int', int: String(amount) } } },
    })
    const finished = await waitForRun(client, run.id!)
    console.log(`example-conditionals amount=${amount} -> ${LHStatus[finished.status]}`)
  }

  await describe.close()
}

main().catch(err => { console.error(err); process.exit(1) })
