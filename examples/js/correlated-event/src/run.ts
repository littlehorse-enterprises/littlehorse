import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

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

function isAlreadyExists(err: unknown): boolean {
  const code = (err as { code?: unknown })?.code
  return code === 'ALREADY_EXISTS' || code === 6
}

async function main() {
  const correlationKey = process.argv[2] ?? 'some-document'

  const run = await client.runWf({
    wfSpecName: 'correlated-events',
    variables: {
      'document-id': { value: { oneofKind: 'str', str: correlationKey } },
    },
  })

  console.log(`posting the document-signed CorrelatedEvent for key "${correlationKey}"...`)
  try {
    await client.putCorrelatedEvent({
      key: correlationKey,
      externalEventDefId: { name: 'document-signed' },
      content: { value: { oneofKind: 'bool', bool: true } },
    })
  } catch (err) {
    // Repeating the put for the same key returns ALREADY_EXISTS; the earlier
    // CorrelatedEvent is still there and correlates this run too.
    if (!isAlreadyExists(err)) throw err
    console.log('CorrelatedEvent already exists for this key; the run correlates against it anyway')
  }

  const finished = await waitForRun(run.id!)
  console.log(`correlated-events -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
