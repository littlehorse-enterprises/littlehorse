import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

function jsonWorkflow() {
  return Workflow.newWorkflow('example-json', thread => {
    const person = thread.declareJsonObj('person')
    thread.execute('greet', person.jsonPath('$.name'))
    thread.execute('describe-car', person.jsonPath('$.car'))
  })
}

const Car = z.object({
  brand: z.string(),
  model: z.string(),
})

async function greet(name: string, _ctx: WorkerContext): Promise<string> {
  console.log(`[greet] name=${name}`)
  return `hello ${name}`
}

async function describeCar(car: z.infer<typeof Car>, _ctx: WorkerContext): Promise<string> {
  console.log(`[describe-car]`, car)
  return `You drive a ${car.brand} ${car.model}`
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const greetWorker = createTaskWorker(greet, 'greet', config, {
    inputVars: { name: z.string() },
  })
  const carWorker = createTaskWorker(describeCar, 'describe-car', config, {
    inputVars: { car: Car },
  })

  const workers = [greetWorker, carWorker]
  for (const w of workers) {
    if (!(await w.doesTaskDefExist())) {
      await w.registerTaskDef()
    }
  }

  console.log('Registering WfSpec "example-json"...')
  await Workflow.registerWfSpec(jsonWorkflow(), client)

  await Promise.all(workers.map(w => w.start()))

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await Promise.all(workers.map(w => w.close()))
    process.exit(0)
  })
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
