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
  const run = await client.runWf({
    wfSpecName: 'example-basic',
    variables: { 'input-name': { value: { oneofKind: 'str', str: 'Obi-Wan' } } },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-basic -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
