import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function someTask(): void {
  console.warn('Executing some-task')
  throw new Error('My task has failed')
}

function myTask(): string {
  console.log('Executing my-task')
  return 'hello, there!'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const my = createTaskWorker(myTask, 'my-task', config, { inputVars: {} })
  const some = createTaskWorker(someTask, 'some-task', config, { inputVars: {} })
  for (const w of [my, some]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-interrupt-handler', thread => {
    // Unlike waitForEvent, an interrupt can fire at any point in the thread.
    thread
      .registerInterruptHandler('interruption-event', handler => {
        handler.execute('some-task')
      })
      .withEventType(z.string())

    // Do some work that takes a while
    thread.sleepSeconds(30)
    thread.execute('my-task')
  })
  await wf.registerWfSpec(config)

  for (const w of [my, some]) await w.start()
  console.log('ready: polling for my-task, some-task tasks')
  console.log('run the workflow:  lhctl run example-interrupt-handler')

  const shutdown = async () => {
    await Promise.all([my.close(), some.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
