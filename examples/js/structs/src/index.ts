import {
  LHConfig,
  createTaskWorker,
  WorkerContext,
  buildPutStructDefRequest,
  getStructDependencies,
  getStructName,
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

  const getCarOwnerWorker = createTaskWorker(getCarOwner, 'get-car-owner', config, {
    inputVars: { report: ParkingTicketReport },
    outputSchema: Person,
  })
  const mailTicketWorker = createTaskWorker(mailTicket, 'mail-ticket', config, {
    inputVars: { person: Person },
  })

  // Register all StructDefs in dependency order
  const seen = new Set<string>()
  const schemasToRegister: typeof Person[] = []
  for (const schema of [...getStructDependencies(Person), ...getStructDependencies(ParkingTicketReport)]) {
    const name = getStructName(schema)!
    if (!seen.has(name)) {
      seen.add(name)
      schemasToRegister.push(schema as typeof Person)
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
