import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function spiderBite(name: string): string {
  console.log('Executing spider-bite')
  if (['Miles', 'Peter'].includes(name)) {
    console.log(`${name} got bitten`)
    return 'Spider-man'
  }
  return `The spider bite has no effect on ${name}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const mutator = createTaskWorker(spiderBite, 'spider-bite', config, {
    inputVars: { name: z.string() },
    outputSchema: z.string(),
  })
  await ensureTaskDef(mutator)

  const wf = Workflow.newWorkflow('example-mutation', thread => {
    const theName = thread.declareStr('name')
    // We pass the name of the person and receive if it is spider-man or not
    const output = thread.execute('spider-bite', theName)

    // We save the output in the variable
    theName.assign(output)
  })
  await wf.registerWfSpec(config)

  await mutator.start()
  console.log('ready: polling for spider-bite tasks')
  console.log('run the workflow:  lhctl run example-mutation name Peter')

  const shutdown = async () => {
    await mutator.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
