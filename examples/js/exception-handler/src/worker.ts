import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { loadConfig } from './config'

const config = loadConfig()

// Throwing a plain Error raises a *technical* ERROR, which handleError below
// catches by spawning the handler thread before execution resumes.
function fail(): string {
  console.log('Executing fail')
  if (Math.random() > 0.5) {
    console.error('There was an error in this task')
    throw new Error('Yikes')
  }
  return 'hi there'
}

function passingTask(): string {
  console.log('Executing my-task')
  return 'woohoo!'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const failWorker = createTaskWorker(fail, 'fail', config, { inputVars: {} })
  const myTask = createTaskWorker(passingTask, 'my-task', config, { inputVars: {} })
  for (const w of [failWorker, myTask]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-exception-handler', thread => {
    const node = thread.execute('fail')

    // Handle technical failure
    thread.handleError(node, null, handler => {
      handler.execute('my-task')
    })

    // Execution resumes after handling exception.
    thread.execute('my-task')
  })
  await wf.registerWfSpec(config)

  for (const w of [failWorker, myTask]) await w.start()
  console.log('ready: polling for fail, my-task tasks')
  console.log('run the workflow:  lhctl run example-exception-handler')

  const shutdown = async () => {
    await Promise.all([failWorker.close(), myTask.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
