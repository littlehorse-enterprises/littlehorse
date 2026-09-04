import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

function askForName(): string {
  console.log('Executing ask-for-name')
  return "Hi what's your name?"
}

function greet(name: string): string {
  console.log('Executing greet')
  return `Hello there, ${name}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const asker = createTaskWorker(askForName, 'ask-for-name', config, { inputVars: {} })
  const greeter = createTaskWorker(greet, 'greet', config, {
    inputVars: { name: z.string() },
  })
  for (const w of [asker, greeter]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-external-event', thread => {
    const name = thread.declareStr('name').searchable()

    thread.execute('ask-for-name')

    // The run parks here until someone posts the event.
    name.assign(thread.waitForEvent('name-event'))

    thread.execute('greet', name)
  })

  // The ExternalEventDef must exist before a WfSpec may reference it.
  for (const externalEventName of wf.getRequiredExternalEventDefNames()) {
    await client.putExternalEventDef({ name: externalEventName })
  }

  await wf.registerWfSpec(config)

  for (const w of [asker, greeter]) await w.start()
  console.log('ready: polling for ask-for-name, greet tasks')
  console.log('run the workflow:  lhctl run example-external-event')

  const shutdown = async () => {
    await Promise.all([asker.close(), greeter.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
