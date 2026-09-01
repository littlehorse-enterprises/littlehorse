import { LHConfig, LHTaskWorker, Workflow, createTaskWorker, userTaskSchema } from 'littlehorse-client'
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
  const finalize = createTaskWorker((approved: boolean) => (approved ? 'approved' : 'rejected'), 'ut-finalize', config, {
    inputVars: { approved: z.boolean() },
  })
  await ensureTaskDef(finalize)
  await finalize.start()

  // The form a human fills in, described with zod.
  // userTaskSchema() builds the schema; compile() turns it into the request.
  // Fields must be renderable primitives — a nested object would silently
  // become JSON that no form can present, so the SDK rejects it.
  await client.putUserTaskDef(
    userTaskSchema('example-approval', { approved: z.boolean(), notes: z.string() }, 'Approve the request').compile()
  )

  const wf = Workflow.newWorkflow('example-user-tasks', thread => {
    const decision = thread.declareBool('decision')
    const form = thread.assignUserTask('example-approval', 'alice', null)
    thread.setUserTaskNotes(form, 'Please review before end of day.')
    decision.assign(form.jsonPath('$.approved'))
    thread.execute('ut-finalize', decision)
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-user-tasks', variables: {} })

  // Poll for the UserTaskRun, then complete it the way a UI would.
  //
  // searchUserTaskRun has no wfRunId filter, so it returns the tasks of every
  // previous run of this example too. Match on our own wfRunId rather than
  // taking results[0] — completing someone else's task would leave this run
  // parked on its UserTask node forever.
  let userTaskId
  for (let i = 0; i < 50 && !userTaskId; i++) {
    const found = await client.searchUserTaskRun({ userTaskDefName: 'example-approval' })
    userTaskId = found.results.find(id => id.wfRunId?.id === run.id!.id)
    if (!userTaskId) await new Promise(r => setTimeout(r, 200))
  }
  await client.completeUserTaskRun({
    userTaskRunId: userTaskId,
    userId: 'alice',
    // Every field the schema declares is mandatory on completion.
    results: {
      approved: { value: { oneofKind: 'bool', bool: true } },
      notes: { value: { oneofKind: 'str', str: 'Looks good to me.' } },
    },
  })

  const finished = await waitForRun(client, run.id!)
  console.log(`example-user-tasks -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await finalize.close()
}

main().catch(err => { console.error(err); process.exit(1) })
