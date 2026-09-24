import { toStructVariableValue } from 'littlehorse-client'
import { LHStatus, WfRunId } from 'littlehorse-client/proto'
import { loadConfig } from './config'
import { ParkingTicketReport } from './schemas'

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

async function main() {
  const report = {
    vehicleMake: 'BARC',
    vehicleModel: 'Speeder',
    licensePlateNumber: '1HGCM82633A004352',
  }
  const run = await client.runWf({
    wfSpecName: 'issue-parking-ticket',
    variables: {
      'ticket-report': toStructVariableValue(report, ParkingTicketReport),
    },
  })
  const finished = await waitForRun(run.id!)
  console.log(`issue-parking-ticket -> ${LHStatus[finished.status]} (${run.id!.id})`)
  config.close()
  if (finished.status !== LHStatus.COMPLETED) process.exit(1)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
