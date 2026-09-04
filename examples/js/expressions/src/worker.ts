import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function placeOrder(total: number): string {
  console.log(`Executing place order with total: ${total}`)
  return `total paid: ${total}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const order = createTaskWorker(placeOrder, 'place-order', config, {
    inputVars: { total: z.number() },
    outputSchema: z.string(),
  })
  await ensureTaskDef(order)

  const wf = Workflow.newWorkflow('example-expressions', thread => {
    const quantity = thread.declareInt('quantity')
    const price = thread.declareDouble('price')
    const taxes = thread.declareDouble('taxes')

    // These build a proto expression tree — the server does the arithmetic.
    thread.execute('place-order', quantity.multiply(price.multiply(thread.add(1, taxes.divide(100.0)))))
  })
  await wf.registerWfSpec(config)

  await order.start()
  console.log('ready: polling for place-order tasks')
  console.log('run the workflow:  lhctl run example-expressions quantity 1 price 0.8 taxes 12')

  const shutdown = async () => {
    await order.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
