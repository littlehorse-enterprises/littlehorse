import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'
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
  const report = createTaskWorker((total: number) => `total is ${total}`, 'expr-report', config, {
    inputVars: { total: z.number() },
  })
  await ensureTaskDef(report)
  await report.start()

  const wf = Workflow.newWorkflow('example-expressions', thread => {
    const price = thread.declareDouble('price').required()
    const quantity = thread.declareInt('quantity').required()
    const total = thread.declareDouble('total')

    // These build a proto expression tree — the server does the arithmetic.
    const subtotal = thread.multiply(price, quantity)
    total.assign(thread.add(subtotal, thread.multiply(subtotal, 0.08)))

    thread.execute('expr-report', total)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({
    wfSpecName: 'example-expressions',
    variables: {
      price: { value: { oneofKind: 'double', double: 19.99 } },
      quantity: { value: { oneofKind: 'int', int: '3' } },
    },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-expressions -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await report.close()
}

main().catch(err => { console.error(err); process.exit(1) })
