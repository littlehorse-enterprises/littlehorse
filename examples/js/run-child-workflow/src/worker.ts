import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function greeting(name: string): string {
  console.log(`Executing task greet: ${name}`)
  return `hello there, ${name}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const greet = createTaskWorker(greeting, 'greet', config, {
    inputVars: { name: z.string() },
    // The child WfSpec returns this task's output, so the TaskDef must
    // declare its STR return type (Java infers it from the method signature).
    outputSchema: z.string(),
  })
  await ensureTaskDef(greet)

  const child = Workflow.newWorkflow('some-other-wfspec', wf => {
    // In the `hierarchical-workflow` example, we require the variable to be INHERITED;
    // however, here the variable is an input.
    const childInputName = wf.declareStr('child-input-name').required()
    wf.complete(wf.execute('greet', childInputName))
  })

  const parent = Workflow.newWorkflow('my-parent', wf => {
    const theName = wf.declareStr('input-name').required()
    const childOutput = wf.declareStr('child-output')

    const spawnedChild = wf.runWf('some-other-wfspec', { 'child-input-name': theName })
    wf.execute('greet', 'hi from parent')

    childOutput.assign(wf.waitForChildWf(spawnedChild))
  })

  await child.registerWfSpec(config)
  await parent.registerWfSpec(config)

  await greet.start()
  console.log('ready: polling for greet tasks')
  console.log('run the workflow:  lhctl run my-parent input-name colt')

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
