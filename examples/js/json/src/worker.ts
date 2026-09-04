import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

type Car = { brand: string; model: string }

function greet(name: string): string {
  console.log('Executing greet')
  return `hello ${name}`
}

function describeCar(car: Car): string {
  console.log(`Executing describe-car. ${JSON.stringify(car)}`)
  return `You drive a ${car.brand} ${car.model}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const greeter = createTaskWorker(greet, 'greet', config, {
    inputVars: { name: z.string() },
    outputSchema: z.string(),
  })
  const describer = createTaskWorker(describeCar, 'describe-car', config, {
    inputVars: { car: z.object({ brand: z.string(), model: z.string() }) },
    outputSchema: z.string(),
  })
  for (const w of [greeter, describer]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-json', thread => {
    const person = thread.declareJsonObj('person')

    // jsonPath reaches into the document; the task receives the extracted value.
    thread.execute('greet', person.jsonPath('$.name'))
    thread.execute('describe-car', person.jsonPath('$.car'))
  })
  await wf.registerWfSpec(config)

  for (const w of [greeter, describer]) await w.start()
  console.log('ready: polling for greet, describe-car tasks')
  console.log(
    `run the workflow:  lhctl run example-json person '{"name": "Obi-Wan", "car": {"brand": "Ford", "model": "Escape"}}'`
  )

  const shutdown = async () => {
    await Promise.all([greeter.close(), describer.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
