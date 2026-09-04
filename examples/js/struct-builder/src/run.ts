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
  const name = 'Obi-Wan'
  const email = 'obi-wan@jedi.org'
  console.log(`Running workflow with name=${name} email=${email}`)

  const run = await client.runWf({
    wfSpecName: 'assemble-person',
    variables: {
      name: { value: { oneofKind: 'str', str: name } },
      email: { value: { oneofKind: 'str', str: email } },
    },
  })
  const finished = await waitForRun(run.id!)
  console.log(`assemble-person -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
