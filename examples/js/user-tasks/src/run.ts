import { LHStatus, UserTaskRunId, WfRunId } from 'littlehorse-client/proto'
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

// searchUserTaskRun has no wfRunId filter, so it returns the tasks of every
// previous run of this example too. Match on our own wfRunId rather than
// taking results[0]; completing someone else's task would leave this run
// parked on its UserTask node forever.
async function findUserTaskRun(userTaskDefName: string, wfRunId: string): Promise<UserTaskRunId> {
  for (let i = 0; i < 50; i++) {
    const found = await client.searchUserTaskRun({ userTaskDefName })
    const userTaskId = found.results.find(id => id.wfRunId?.id === wfRunId)
    if (userTaskId) return userTaskId
    await new Promise(r => setTimeout(r, 200))
  }
  throw new Error(`No UserTaskRun of '${userTaskDefName}' appeared for this run`)
}

async function main() {
  const run = await client.runWf({
    wfSpecName: 'it-request',
    variables: { 'user-id': { value: { oneofKind: 'str', str: 'anakin' } } },
  })

  // Fill in the IT request form as anakin, the way a UI would.
  const requestFormId = await findUserTaskRun('it-request', run.id!.id)
  await client.completeUserTaskRun({
    userTaskRunId: requestFormId,
    userId: 'anakin',
    results: {
      requestedItem: { value: { oneofKind: 'str', str: 'the rank of master' } },
      justification: {
        value: { oneofKind: 'str', str: "it's not fair to be on this council and not be a Master!" },
      },
    },
  })

  // The approval form arrives unassigned to the finance group; claim it as
  // mace and answer it (if you know Star Wars, you know the correct answer).
  const approvalFormId = await findUserTaskRun('approve-it-request', run.id!.id)
  await client.assignUserTaskRun({ userTaskRunId: approvalFormId, userId: 'mace', overrideClaim: false })
  await client.completeUserTaskRun({
    userTaskRunId: approvalFormId,
    userId: 'mace',
    results: { isApproved: { value: { oneofKind: 'bool', bool: false } } },
  })

  const finished = await waitForRun(run.id!)
  console.log(`it-request -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
