import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function eatingDonut(donutsLeft: number): string {
  const left = donutsLeft - 1
  const message = `eating donut, ${left} left`
  console.log(message)
  return message
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const eater = createTaskWorker(eatingDonut, 'eating-donut', config, {
    inputVars: { donutsLeft: z.number().int() },
  })
  await ensureTaskDef(eater)

  const wf = Workflow.newWorkflow('example-conditionals-while', thread => {
    const numDonuts = thread.declareInt('number-of-donuts').required()

    thread.doWhile(numDonuts.isGreaterThan(0), handler => {
      // The engine evaluates the mutation; nothing loops in this JS process.
      numDonuts.assign(numDonuts.subtract(1))
      handler.execute('eating-donut', numDonuts)
    })
  })
  await wf.registerWfSpec(config)

  await eater.start()
  console.log('ready: polling for eating-donut tasks')
  console.log('run the workflow:  lhctl run example-conditionals-while number-of-donuts 5')

  const shutdown = async () => {
    await eater.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
