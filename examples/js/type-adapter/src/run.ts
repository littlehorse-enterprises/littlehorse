import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

const UUID_FORMAT = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i

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
  const run = await client.runWf({ wfSpecName: 'example-type-adapter', variables: {} })
  const finished = await waitForRun(run.id!)

  // The Java README's verification: get-uuid stored a UUID-like string value.
  const uuidVar = await client.getVariable({ wfRunId: run.id, threadRunNumber: 0, name: 'uuid' })
  const uuid = uuidVar.value?.value.oneofKind === 'str' ? uuidVar.value.value.str : undefined

  console.log(`example-type-adapter -> ${LHStatus[finished.status]} (${run.id!.id}) uuid=${uuid}`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED || uuid === undefined || !UUID_FORMAT.test(uuid)) {
    process.exit(1)
  }
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
