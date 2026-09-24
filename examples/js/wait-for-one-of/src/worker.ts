import { LHTaskWorker, Workflow, createTaskWorker, spawnedThreadsOf } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

function childCompleted(): string {
  console.log('One of the child threads has completed!')
  return 'Child thread execution completed successfully'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const completed = createTaskWorker(childCompleted, 'child-completed', config, {
    inputVars: {},
    outputSchema: z.string(),
  })
  await ensureTaskDef(completed)

  const wf = Workflow.newWorkflow('example-wait-for-one-of', thread => {
    // Spawn two child threads that wait for different external events
    const childThread1 = thread.spawnThread(
      child => {
        child.waitForEvent('child-1-event')
      },
      'child-1',
      {}
    )

    const childThread2 = thread.spawnThread(
      child => {
        child.waitForEvent('child-2-event')
      },
      'child-2',
      {}
    )

    // Wait for any one of the child threads to complete
    thread.waitForAnyOf(spawnedThreadsOf(childThread1, childThread2))

    // Execute a task after one child completes
    thread.execute('child-completed')
  })

  // The ExternalEventDefs must exist before the WfSpec may reference them.
  for (const externalEventName of wf.getRequiredExternalEventDefNames()) {
    await client.putExternalEventDef({ name: externalEventName })
  }
  await wf.registerWfSpec(config)

  await completed.start()
  console.log('ready: polling for child-completed tasks')
  console.log('run the workflow:  lhctl run example-wait-for-one-of --wfRunId my-wf-run')

  const shutdown = async () => {
    await completed.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
