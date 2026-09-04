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

async function main() {
  const run = await client.runWf({ wfSpecName: 'example-wait-for-one-of', variables: {} })
  console.log('posting child-1-event; child-2 stays parked and gets halted...')
  await client.putExternalEvent({
    wfRunId: run.id,
    externalEventDefId: { name: 'child-1-event' },
    content: { value: { oneofKind: 'str', str: 'hello' } },
  })

  const finished = await waitForRun(run.id!)
  console.log(`example-wait-for-one-of -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
