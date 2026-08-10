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
  const greeter = createTaskWorker((name: string) => `Hello, ${name}!`, 'basic-greet', config, {
    inputVars: { name: z.string() },
  })
  await ensureTaskDef(greeter)
  await greeter.start()

  // The wfsdk compiles this closure into a PutWfSpecRequest. It never runs the
  // workflow itself — the server does that.
  const wf = Workflow.newWorkflow('example-basic', thread => {
    const name = thread.declareStr('name').required()
    const greeting = thread.declareStr('greeting')
    greeting.assign(thread.execute('basic-greet', name))
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({
    wfSpecName: 'example-basic',
    variables: { name: { value: { oneofKind: 'str', str: 'LittleHorse' } } },
  })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-basic -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await greeter.close()
}

main().catch(err => { console.error(err); process.exit(1) })
