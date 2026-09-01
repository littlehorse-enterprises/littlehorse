import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { z } from 'zod'

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
  const work = createTaskWorker((who: string) => `child did work for ${who}`, 'cw-work', config, {
    inputVars: { who: z.string() },
  })
  await ensureTaskDef(work)
  await work.start()

  // The child is an ordinary WfSpec — nothing marks it as "child only".
  const child = Workflow.newWorkflow('example-child-workflow-child', thread => {
    const who = thread.declareStr('who').required()
    thread.execute('cw-work', who)
  })
  await child.registerWfSpec(config)

  const parent = Workflow.newWorkflow('example-child-workflow', thread => {
    const spawned = thread.runWf('example-child-workflow-child', { who: 'the parent' })
    thread.waitForChildWf(spawned)
  })
  await parent.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-child-workflow', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-child-workflow -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await work.close()
}

main().catch(err => { console.error(err); process.exit(1) })
