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
  const ship = createTaskWorker((city: string, sku: string) => `shipping ${sku} to ${city}`, 'json-ship', config, {
    inputVars: { city: z.string(), sku: z.string() },
  })
  await ensureTaskDef(ship)
  await ship.start()

  const wf = Workflow.newWorkflow('example-json', thread => {
    const order = thread.declareJsonObj('order').required()
    const summary = thread.declareStr('summary')

    // jsonPath reaches into the document; get() indexes a field or array slot.
    const city = order.jsonPath('$.address.city')
    const firstSku = order.jsonPath('$.items[0].sku')

    summary.assign(thread.execute('json-ship', city, firstSku))
  })
  await wf.registerWfSpec(config)

  const order = { address: { city: 'Austin' }, items: [{ sku: 'LH-1' }, { sku: 'LH-2' }] }
  const run = await client.runWf({
    wfSpecName: 'example-json',
    variables: { order: { value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify(order) } } },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-json -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await ship.close()
}

main().catch(err => { console.error(err); process.exit(1) })
