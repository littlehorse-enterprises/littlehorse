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
  const approvalChain = {
    description: 'demo for approvals',
    approvals: [{ user: 'yoda' }, { user: 'chewbacca' }, { user: 'anakin' }],
  }
  const run = await client.runWf({
    wfSpecName: 'spawn-parallel-threads-from-json-arr-variable',
    variables: { 'approval-chain': { value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify(approvalChain) } } },
  })
  const finished = await waitForRun(run.id!)
  console.log(
    `spawn-parallel-threads-from-json-arr-variable -> ${LHStatus[finished.status]}, ${finished.threadRuns.length} threadRuns`
  )
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
