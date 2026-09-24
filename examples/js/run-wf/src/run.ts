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
  // Java submits runs on a timer with an incrementing n; a trigger script
  // does the same, bounded so it exits.
  let failed = false
  for (let n = 1; n <= 5; n++) {
    console.log(`Requesting wf run execution, n = ${n}`)
    const run = await client.runWf({
      wfSpecName: 'example-run-wf',
      variables: { n: { value: { oneofKind: 'int', int: String(n) } } },
    })
    const finished = await waitForRun(run.id!)
    console.log(`example-run-wf n=${n} -> ${LHStatus[finished.status]} (${run.id!.id})`)
    if (finished.status !== LHStatus.COMPLETED) failed = true
  }
  config.close()
  if (failed) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
