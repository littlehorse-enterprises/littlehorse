import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

function mutationWorkflow() {
  return Workflow.newWorkflow('example-mutation', thread => {
    const name = thread.declareStr('name')
    const out = thread.execute('spider-bite', name)
    name.assign(out)
  })
}

async function spiderBite(name: string, _ctx: WorkerContext): Promise<string> {
  const heroes = ['Miles', 'Peter']
  if (heroes.includes(name)) {
    console.log(`[spider-bite] ${name} got bitten`)
    return 'Spider-man'
  }
  return `The spider bite has no effect on ${name}`
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const worker = createTaskWorker(spiderBite, 'spider-bite', config, {
    inputVars: { name: z.string() },
  })

  if (!(await worker.doesTaskDefExist())) {
    await worker.registerTaskDef()
  }

  console.log('Registering WfSpec "example-mutation"...')
  await Workflow.registerWfSpec(mutationWorkflow(), client)

  await worker.start()

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await worker.close()
    process.exit(0)
  })
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
