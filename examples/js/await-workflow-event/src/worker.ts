import { Workflow } from 'littlehorse-client'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

async function main() {
  await client.putWorkflowEventDef({ name: 'sleep-done' })

  const wf = Workflow.newWorkflow('await-wf-event', thread => {
    const sleepTime = thread.declareInt('sleep-time').required()
    thread.sleepSeconds(sleepTime)
    thread.throwEvent('sleep-done', 'hello there!')
  })
  await wf.registerWfSpec(config)

  console.log('ready: no TaskDefs here, the WfSpec just sleeps and throws a WorkflowEvent')
  console.log('run the workflow:  lhctl run await-wf-event sleep-time 1')

  // No task workers to poll, so hold the process open until it is stopped.
  const keepAlive = setInterval(() => {}, 60_000)

  const shutdown = async () => {
    clearInterval(keepAlive)
    config.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
