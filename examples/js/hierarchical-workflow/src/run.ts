import { LHStatus, Variable, WfRunId } from 'littlehorse-client/proto'
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

function strValue(variable: Variable): string | undefined {
  const value = variable.value?.value
  return value?.oneofKind === 'str' ? value.str : undefined
}

async function main() {
  const parentRun = await client.runWf({
    wfSpecName: 'parent',
    variables: { name: { value: { oneofKind: 'str', str: 'obi-wan' } } },
  })
  const parentFinished = await waitForRun(parentRun.id!)
  const nameBefore = strValue(await client.getVariable({ wfRunId: parentRun.id, threadRunNumber: 0, name: 'name' }))
  console.log(`parent -> ${LHStatus[parentFinished.status]}, name = ${nameBefore}`)

  const childRun = await client.runWf({ wfSpecName: 'child', variables: {}, parentWfRunId: parentRun.id })
  const childFinished = await waitForRun(childRun.id!)
  const nameAfter = strValue(await client.getVariable({ wfRunId: parentRun.id, threadRunNumber: 0, name: 'name' }))
  console.log(`child -> ${LHStatus[childFinished.status]}, parent name = ${nameAfter}`)

  const grandChildRun = await client.runWf({ wfSpecName: 'grand-child', variables: {}, parentWfRunId: childRun.id })
  console.log('posting some-event to the grand-child...')
  await client.putExternalEvent({
    wfRunId: grandChildRun.id,
    externalEventDefId: { name: 'some-event' },
    content: { value: { oneofKind: 'str', str: '{"message": "Hello from grand-child!"}' } },
  })
  const grandChildFinished = await waitForRun(grandChildRun.id!)

  console.log(`grand-child -> ${LHStatus[grandChildFinished.status]} (${grandChildRun.id!.id})`)
  config.close()
  const succeeded =
    nameBefore === 'obi-wan' && nameAfter === 'yoda' && grandChildFinished.status === LHStatus.COMPLETED
  if (!succeeded) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
