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
  const run = await client.runWf({ wfSpecName: 'parallel-approval', variables: {} })
  console.log('posting the three approval events...')
  for (const eventName of ['person-2-approves', 'person-1-approves', 'person-3-approves']) {
    await client.putExternalEvent({
      wfRunId: run.id,
      externalEventDefId: { name: eventName },
      content: { value: { oneofKind: 'jsonObj', jsonObj: '{"approval": true}' } },
    })
  }

  // The reminder thread sleeps about 20 seconds before it notices all-approved.
  const finished = await waitForRun(run.id!)
  console.log(`parallel-approval -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
