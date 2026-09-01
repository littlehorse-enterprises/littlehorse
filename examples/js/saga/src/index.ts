import { LHConfig, LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { LHTaskException } from 'littlehorse-client'

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
  const reserve = createTaskWorker(() => 'seat reserved', 'saga-reserve-seat', config, { inputVars: {} })
  const bookHotel = createTaskWorker(() => {
    // Fails deliberately, so the compensation path is what runs.
    throw new LHTaskException('hotel-unavailable', 'no rooms left')
  }, 'saga-book-hotel', config, { inputVars: {} })
  const cancelSeat = createTaskWorker(() => 'seat released', 'saga-cancel-seat', config, { inputVars: {} })
  for (const w of [reserve, bookHotel, cancelSeat]) { await ensureTaskDef(w); await w.start() }

  const wf = Workflow.newWorkflow('example-saga', thread => {
    thread.execute('saga-reserve-seat')

    const hotel = thread.execute('saga-book-hotel')
    // When booking the hotel fails, undo the seat reservation.
    thread.handleException(hotel, 'hotel-unavailable', handler => {
      handler.execute('saga-cancel-seat')
    })
  })
  await wf.registerWfSpec(config)

  const run = await client.runWf({ wfSpecName: 'example-saga', variables: {} })
  const finished = await waitForRun(client, run.id!)
  console.log(`example-saga -> ${LHStatus[finished.status]} (${run.id!.id})`)

  await Promise.all([reserve.close(), bookHotel.close(), cancelSeat.close()])
}

main().catch(err => { console.error(err); process.exit(1) })
