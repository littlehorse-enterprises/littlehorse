import { LHStatus, ThreadType, WfRunId } from 'littlehorse-client/proto'
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
  const run = await client.runWf({ wfSpecName: 'example-interrupt-handler', variables: {} })
  await new Promise(r => setTimeout(r, 500))
  console.log('interrupting the run...')
  await client.putExternalEvent({
    wfRunId: run.id,
    externalEventDefId: { name: 'interruption-event' },
    content: { value: { oneofKind: 'str', str: 'hello' } },
  })

  // The interrupt handler's some-task fails, so threadRun 1 ends up ERROR with
  // type INTERRUPT and the whole workflow fails.
  const finished = await waitForRun(run.id!)
  const interrupt = finished.threadRuns[1]
  console.log(
    `example-interrupt-handler -> ${LHStatus[finished.status]}, ` +
      `threadRun 1 is ${interrupt === undefined ? 'missing' : `${LHStatus[interrupt.status]} with type ${ThreadType[interrupt.type]}`}`
  )
  config.close()
  const ok =
    finished.status === LHStatus.ERROR &&
    interrupt !== undefined &&
    interrupt.type === ThreadType.INTERRUPT &&
    interrupt.status === LHStatus.ERROR
  if (!ok) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
