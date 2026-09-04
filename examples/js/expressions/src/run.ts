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
  const run = await client.runWf({
    wfSpecName: 'example-expressions',
    variables: {
      quantity: { value: { oneofKind: 'int', int: '1' } },
      price: { value: { oneofKind: 'double', double: 0.8 } },
      taxes: { value: { oneofKind: 'double', double: 12 } },
    },
  })
  const finished = await waitForRun(run.id!)
  console.log(`example-expressions -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
