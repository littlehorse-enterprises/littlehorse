import { LHConfig, LHTaskWorker, Workflow, createTaskWorker, spawnedThreadsOf } from 'littlehorse-client'
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
  const notify = createTaskWorker((to: string) => `notified ${to}`, 'ct-notify', config, {
    inputVars: { to: z.string() },
  })
  const audit = createTaskWorker(() => 'audited', 'ct-audit', config, { inputVars: {} })
  for (const w of [notify, audit]) { await ensureTaskDef(w); await w.start() }

  const wf = Workflow.newWorkflow('example-child-thread', thread => {
    // Each spawned thread is a real ThreadRun on the server, running in parallel.
    const notifier = thread.spawnThread(child => {
      const to = child.addVariable('to', 'someone')
      child.execute('ct-notify', to)
    }, 'notifier', { to: 'ops@littlehorse.io' })

    const auditor = thread.spawnThread(child => child.execute('ct-audit'), 'auditor', {})

    thread.waitForThreads(spawnedThreadsOf(notifier, auditor))
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-child-thread', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-child-thread -> ${LHStatus[finished.status]}, ${finished.threadRuns.length} threadRuns`)

  await Promise.all([notify.close(), audit.close()])
}

main().catch(err => { console.error(err); process.exit(1) })
