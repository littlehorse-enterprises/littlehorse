import { Workflow } from 'littlehorse-client'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

const INTERRUPT_NAME = 'subtract'

async function main() {
  await client.putExternalEventDef({ name: INTERRUPT_NAME })

  const wf = Workflow.newWorkflow('example-wait-for-condition', thread => {
    const counter = thread.declareInt('counter').withDefault(2)

    thread.waitForCondition(counter.isEqualTo(0))

    // Interrupt handler which mutates the parent variable
    thread.registerInterruptHandler(INTERRUPT_NAME, handler => {
      counter.assign(counter.subtract(1))
    })
  })
  await wf.registerWfSpec(config)

  // This example has no TaskDefs and therefore no task workers: runs park on
  // the WAIT_FOR_CONDITION node until enough subtract events arrive.
  console.log('ready: waiting for subtract events (no task workers in this example)')
  console.log('run the workflow:  lhctl run example-wait-for-condition counter 1')

  const keepAlive = setInterval(() => {}, 60_000)
  const shutdown = async () => {
    clearInterval(keepAlive)
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
