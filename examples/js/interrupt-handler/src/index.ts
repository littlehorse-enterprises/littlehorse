import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'

const config = LHConfig.from({})
const client = config.getClient()

/** Polls until the WfRun reaches a terminal status, then returns it. */
async function waitForRun(client: ReturnType<LHConfig['getClient']>, id: WfRunId) {
  for (let i = 0; i < 150; i++) {
    const run = await client.getWfRun(id)
    if (run.status === LHStatus.COMPLETED || run.status === LHStatus.ERROR || run.status === LHStatus.EXCEPTION) {
      return run
    }
    await new Promise(r => setTimeout(r, 200))
  }
  throw new Error('WfRun did not finish in time')
}

/** Registers the TaskDef behind a worker if the server does not have it yet. */
async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const cleanup = createTaskWorker(() => 'cleaned up', 'int-cleanup', config, { inputVars: {} })
  const work = createTaskWorker(() => 'long work finished', 'int-work', config, { inputVars: {} })
  for (const w of [cleanup, work]) { await ensureTaskDef(w); await w.start() }

  await client.putExternalEventDef({ name: 'example-cancel-requested', contentType: {} })

  const wf = Workflow.newWorkflow('example-interrupt-handler', thread => {
    // Unlike waitForEvent, an interrupt can fire at any point in the thread.
    thread.registerInterruptHandler('example-cancel-requested', handler => {
      handler.execute('int-cleanup')
    })
    thread.sleepSeconds(3)
    thread.execute('int-work')
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-interrupt-handler', variables: {} })
  await new Promise(r => setTimeout(r, 500))
  console.log('interrupting the run...')
  await client.putExternalEvent({
    wfRunId: run.id,
    externalEventDefId: { name: 'example-cancel-requested' },
    content: { value: { oneofKind: 'str', str: 'user cancelled' } },
  })

  const finished = await waitForRun(client, run.id!)
  console.log(`example-interrupt-handler -> ${LHStatus[finished.status]}, ${finished.threadRuns.length} threadRuns`)

  await Promise.all([cleanup.close(), work.close()])
}

main().catch(err => { console.error(err); process.exit(1) })
