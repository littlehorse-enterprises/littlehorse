import { Workflow } from 'littlehorse-client'
import { VariableType } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

const WF_NAME = 'correlated-events'
const EVENT_NAME = 'document-signed'

async function main() {
  await client.putExternalEventDef({
    name: EVENT_NAME,
    contentType: {
      returnType: { definedType: { oneofKind: 'primitiveType', primitiveType: VariableType.BOOL }, masked: false },
    },
    correlatedEventConfig: { deleteAfterFirstCorrelation: false },
  })

  const wf = Workflow.newWorkflow(WF_NAME, thread => {
    const documentId = thread.declareStr('document-id')
    thread.waitForEvent(EVENT_NAME).withCorrelationId(documentId)
  })
  await wf.registerWfSpec(config)

  console.log('ready: registered the ExternalEventDef and the WfSpec (this example has no task workers)')
  console.log('run the workflow:  lhctl run correlated-events document-id my-document-id-asdf')

  // Nothing polls here, so an idle timer keeps the process alive until a signal.
  const keepAlive = setInterval(() => {}, 60 * 1000)
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
