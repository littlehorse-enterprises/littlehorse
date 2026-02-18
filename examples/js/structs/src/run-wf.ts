import { LHConfig, toStructVariableValue } from 'littlehorse-client'
import { ParkingTicketReport } from './schemas.js'

/**
 * Runs the "issue-parking-ticket" workflow, passing a ParkingTicketReport struct as input.
 *
 * Usage:
 *   npm run run-wf -- <vehicleMake> <vehicleModel> <licensePlateNumber>
 */
async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const [vehicleMake, vehicleModel, licensePlateNumber] = process.argv.slice(2)

  if (!vehicleMake || !vehicleModel || !licensePlateNumber) {
    console.error('Usage: npm run run-wf -- <vehicleMake> <vehicleModel> <licensePlateNumber>')
    process.exit(1)
  }

  const report = {
    vehicleMake,
    vehicleModel,
    licensePlateNumber,
    reportedAt: new Date().toISOString(),
  }

  console.log('Generated parking ticket report:', report)

  const reportValue = toStructVariableValue(report, ParkingTicketReport)

  const result = await client.runWf({
    wfSpecName: 'issue-parking-ticket',
    variables: { 'car-input': reportValue },
  })

  console.log(`Started WfRun: ${result.id?.id}`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
