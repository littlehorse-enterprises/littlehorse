import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { Comparator, LHStatus, VariableMutationType, WfRunId } from 'littlehorse-client/proto'
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
  const step = createTaskWorker((i: number) => `iteration ${i}`, 'while-step', config, {
    inputVars: { i: z.number().int() },
  })
  await ensureTaskDef(step)
  await step.start()

  const wf = Workflow.newWorkflow('example-conditionals-while', thread => {
    const counter = thread.declareInt('counter').withDefault(0)

    thread.doWhile(thread.condition(counter, Comparator.LESS_THAN, 5), loop => {
      loop.execute('while-step', counter)
      // The engine evaluates the mutation; nothing loops in this JS process.
      loop.mutate(counter, VariableMutationType.ADD, 1)
    })
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-conditionals-while', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-conditionals-while -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await step.close()
}

main().catch(err => { console.error(err); process.exit(1) })
