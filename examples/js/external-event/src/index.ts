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
  const confirm = createTaskWorker((code: string) => `confirmed with ${code}`, 'ee-confirm', config, {
    inputVars: { code: z.string() },
  })
  await ensureTaskDef(confirm)
  await confirm.start()

  // The ExternalEventDef must exist before a WfSpec may reference it.
  await client.putExternalEventDef({ name: 'example-payment-received', contentType: {} })

  const wf = Workflow.newWorkflow('example-external-event', thread => {
    const code = thread.declareStr('code')
    // The run parks here until someone posts the event.
    code.assign(thread.waitForEvent('example-payment-received'))
    thread.execute('ee-confirm', code)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-external-event', variables: {} })
  console.log('posting the external event...')
  await client.putExternalEvent({
    wfRunId: run.id,
    externalEventDefId: { name: 'example-payment-received' },
    content: { value: { oneofKind: 'str', str: 'PAY-123' } },
  })

  const finished = await waitForRun(client, run.id!)
  console.log(`example-external-event -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await confirm.close()
}

main().catch(err => { console.error(err); process.exit(1) })
