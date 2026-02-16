import {
  LHConfig,
  LHTaskWorker,
  WorkerContext,
  buildPutStructDefRequest,
  buildStructVariableDef,
  getStructDependencies,
  type LHStructSchema,
} from 'littlehorse-client'
import { Address, Person, ParkingTicketReport } from './schemas.js'
import type { Person as PersonType, ParkingTicketReport as ParkingTicketReportType } from './schemas.js'

/**
 * Task: given a ParkingTicketReport, look up and return the car owner.
 * Returns a plain Person object — the worker's outputSchema handles serialization.
 */
function getCarOwner(report: ParkingTicketReportType, _ctx: WorkerContext): PersonType {
  console.log(`[get-car-owner] Looking up owner for plate: ${report.licensePlateNumber}`)
  return {
    firstName: 'Obi-Wan',
    lastName: 'Kenobi',
    homeAddress: { houseNumber: 124, street: 'Sand Dune Lane', city: 'Anchorhead', state: 'Tattooine', zip: 97412 },
  }
}

/**
 * Task: given a Person, "mail" them a parking ticket.
 */
function mailTicket(person: PersonType, _ctx: WorkerContext): string {
  console.log(`[mail-ticket] Notifying ${person.firstName} ${person.lastName} of parking ticket.`)
  return `Ticket sent to ${person.firstName} ${person.lastName} at ${person.homeAddress?.houseNumber} ${person.homeAddress?.street}`
}

async function main() {
  const config = LHConfig.from({})

  const getCarOwnerWorker = new LHTaskWorker(getCarOwner, 'get-car-owner', config, {
    inputVars: [buildStructVariableDef('report', ParkingTicketReport)],
    outputSchema: Person,
  })
  const mailTicketWorker = new LHTaskWorker(mailTicket, 'mail-ticket', config, {
    inputVars: [buildStructVariableDef('person', Person)],
  })

  // Register all StructDefs in dependency order
  const seen = new Set<string>()
  const schemasToRegister: LHStructSchema[] = []
  for (const schema of [...getStructDependencies(Person), ...getStructDependencies(ParkingTicketReport)]) {
    if (!seen.has(schema.name)) {
      seen.add(schema.name)
      schemasToRegister.push(schema)
    }
  }

  for (const schema of schemasToRegister) {
    await getCarOwnerWorker.registerStructDef(buildPutStructDefRequest(schema))
  }

  // Register TaskDefs
  console.log('Registering TaskDef "get-car-owner"...')
  await getCarOwnerWorker.registerTaskDef()
  console.log('Registering TaskDef "mail-ticket"...')
  await mailTicketWorker.registerTaskDef()

  // Start both workers
  await getCarOwnerWorker.start()
  await mailTicketWorker.start()

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await Promise.all([getCarOwnerWorker.close(), mailTicketWorker.close()])
    process.exit(0)
  })
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
