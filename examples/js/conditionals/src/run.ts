import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
type Client = Awaited<ReturnType<typeof config.getAuthenticatedClient>>

async function waitForRun(client: Client, id: WfRunId) {
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
  const client = await config.getAuthenticatedClient()
  let ok = true
  // bar > 10 executes task-b; otherwise task-c.
  for (const bar of [15, 5]) {
    const run = await client.runWf({
      wfSpecName: 'example-conditionals',
      variables: { foo: { value: { oneofKind: 'jsonObj', jsonObj: JSON.stringify({ bar }) } } },
    })
    const finished = await waitForRun(client, run.id!)
    console.log(`example-conditionals foo={"bar": ${bar}} -> ${LHStatus[finished.status]}`)
    if (finished.status !== LHStatus.COMPLETED) ok = false
  }
  config.close()
  if (!ok) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
