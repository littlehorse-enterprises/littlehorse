import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, VariableType, WfRunId } from 'littlehorse-client/proto'
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
  const handle = createTaskWorker((item: string) => `processed ${item}`, 'foreach-handle', config, {
    inputVars: { item: z.string() },
  })
  await ensureTaskDef(handle)
  await handle.start()

  const wf = Workflow.newWorkflow('example-spawn-thread-foreach', thread => {
    const items = thread.declareJsonArr('items').required()

    // One ThreadRun per element; INPUT is bound to the element automatically.
    const children = thread.spawnThreadForEach(items, 'processor', child => {
      const input = child.addVariable('INPUT', VariableType.STR)
      child.execute('foreach-handle', input)
    })
    thread.waitForThreads(children)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({
    wfSpecName: 'example-spawn-thread-foreach',
    variables: { items: { value: { oneofKind: 'jsonArr', jsonArr: JSON.stringify(['a', 'b', 'c']) } } },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-spawn-thread-foreach -> ${LHStatus[finished.status]}, ${finished.threadRuns.length} threadRuns`)

  await handle.close()
}

main().catch(err => { console.error(err); process.exit(1) })
