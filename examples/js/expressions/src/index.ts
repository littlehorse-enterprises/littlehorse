import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

function expressionsWorkflow() {
  return Workflow.newWorkflow('example-expressions', thread => {
    const quantity = thread.declareInt('quantity')
    const price = thread.declareDouble('price')
    const taxes = thread.declareDouble('taxes')
    const lineTotal = quantity.multiply(price.multiply(taxes.divide(100).add(1)))
    thread.execute('place-order', lineTotal)
  })
}

async function placeOrder(total: number, ctx: WorkerContext): Promise<string> {
  const msg = `total paid: ${total}`
  ctx.log(msg)
  console.log(`[place-order] ${msg}`)
  return msg
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const worker = createTaskWorker(placeOrder, 'place-order', config, {
    inputVars: { total: z.number() },
  })

  if (!(await worker.doesTaskDefExist())) {
    console.log('TaskDef "place-order" not found, registering...')
    await worker.registerTaskDef()
  }

  console.log('Registering WfSpec "example-expressions"...')
  await Workflow.registerWfSpec(expressionsWorkflow(), client)

  await worker.start()

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await worker.close()
    process.exit(0)
  })
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
