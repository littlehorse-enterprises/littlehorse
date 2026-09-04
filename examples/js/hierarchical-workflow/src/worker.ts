import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function greeting(name: string): string {
  console.log('Executing task greet: ' + name)
  return 'hello there, ' + name
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const greet = createTaskWorker(greeting, 'greet', config, {
    inputVars: { name: z.string() },
    outputSchema: z.string(),
  })
  await ensureTaskDef(greet)

  const parent = Workflow.newWorkflow('parent', wf => {
    const theName = wf.declareStr('name').asPublic()

    wf.execute('greet', theName)
  })

  const child = Workflow.newWorkflow('child', wf => {
    // Refers to the variable defined in the parent WfSpec.
    const theName = wf.declareStr('name').asInherited()

    wf.execute('greet', theName)

    theName.assign('yoda')
  })
  child.setParent('parent')

  const grandChild = Workflow.newWorkflow('grand-child', wf => {
    wf.waitForEvent('some-event').registeredAs(z.string())
  })
  grandChild.setParent('child')

  await parent.registerWfSpec(config)
  await child.registerWfSpec(config)
  await grandChild.registerWfSpec(config)

  await greet.start()
  console.log('ready: polling for greet tasks')
  console.log('run the workflow:  lhctl run parent name obi-wan --wfRunId my-parent-wf')

  const shutdown = async () => {
    await greet.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
