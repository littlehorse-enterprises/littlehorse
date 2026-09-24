import { randomUUID } from 'node:crypto'
import { LHStatus, WfRunId, WorkflowEvent } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

// The "WorkflowEvent arrives first" scenario from the Java README:
// run with sleep-time 1, wait 3000ms, then await with a 1000ms timeout.
const delayMs = 3000
const timeoutMs = 1000
const sleepSeconds = 1

async function waitForRun(id: WfRunId) {
  for (let i = 0; i < 150; i++) {
    const run = await client.getWfRun(id)
    if (run.status === LHStatus.COMPLETED || run.status === LHStatus.ERROR || run.status === LHStatus.EXCEPTION) {
      return run
    }
    await new Promise(r => setTimeout(r, 200))
  }
  throw new Error('WfRun did not finish in time')
}

async function main() {
  const id = randomUUID().replaceAll('-', '')

  console.log(`Running workflow with id: ${id}`)
  const run = await client.runWf({
    wfSpecName: 'await-wf-event',
    id,
    variables: {
      'sleep-time': { value: { oneofKind: 'int', int: String(sleepSeconds) } },
    },
  })

  console.log(`Sleeping for ${delayMs} milliseconds`)
  await new Promise(r => setTimeout(r, delayMs))

  console.log(`Now awaiting workflow event with timeout of ${timeoutMs} milliseconds`)
  // The server matches waiters per WorkflowEventDefId, so name the event
  // being awaited; an empty eventDefIds list never matches anything.
  const event = await client.awaitWorkflowEvent(
    { wfRunId: { id }, eventDefIds: [{ name: 'sleep-done' }] },
    { timeout: timeoutMs }
  )

  console.log(WorkflowEvent.toJsonString(event, { prettySpaces: 2 }))

  const finished = await waitForRun(run.id!)
  console.log(`await-wf-event -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
