import {
  LHConfig,
  LHTaskWorker,
  WorkerContext,
  buildPutStructDefRequest,
  buildStructVariableDef,
  getStructDependencies,
} from 'littlehorse-client'
import { ParkingTicketReport } from './parking-ticket-report.js'
import { Person } from './person.js'
import { Address } from './address.js'

/**
 * Task: given a ParkingTicketReport, look up and return the car owner.
 * Returns a Person instance so the SDK automatically serializes it as a Struct.
 */
function getCarOwner(report: ParkingTicketReport, _ctx: WorkerContext): Person {
  console.log(`[get-car-owner] Looking up owner for plate: ${report.licensePlateNumber}`)
  // Simulate a database lookup
  return new Person(
    'Obi-Wan',
    'Kenobi',
    new Address(124, 'Sand Dune Lane', 'Anchorhead', 'Tattooine', 97412)
  )
}

/**
 * Task: given a Person, "mail" them a parking ticket.
 */
function mailTicket(person: Person, _ctx: WorkerContext): string {
  console.log(`[mail-ticket] Notifying ${person.firstName} ${person.lastName} of parking ticket.`)
  return `Ticket sent to ${person.firstName} ${person.lastName} at ${person.homeAddress?.houseNumber} ${person.homeAddress?.street}`
}

async function main() {
  const config = LHConfig.from({})

  // Build input var definitions for each task
  const getCarOwnerInputVars = [buildStructVariableDef('report', ParkingTicketReport)]
  const mailTicketInputVars = [buildStructVariableDef('person', Person)]

  const getCarOwnerWorker = new LHTaskWorker(getCarOwner, 'get-car-owner', config, {
    inputVars: getCarOwnerInputVars,
  })
  const mailTicketWorker = new LHTaskWorker(mailTicket, 'mail-ticket', config, {
    inputVars: mailTicketInputVars,
  })

  // 1. Register all StructDefs in dependency order.
  //    Person depends on Address, so getStructDependencies(Person) returns [Address, Person].
  //    We union all dependencies across all struct classes used.
  const allStructClasses = new Set<new (...args: any[]) => any>()
  for (const cls of getStructDependencies(Person)) allStructClasses.add(cls)
  for (const cls of getStructDependencies(ParkingTicketReport)) allStructClasses.add(cls)

  for (const cls of allStructClasses) {
    const req = buildPutStructDefRequest(cls)
    await getCarOwnerWorker.registerStructDef(req)
  }

  // 2. Register TaskDefs
  console.log('Registering TaskDef "get-car-owner"...')
  await getCarOwnerWorker.registerTaskDef()
  console.log('Registering TaskDef "mail-ticket"...')
  await mailTicketWorker.registerTaskDef()

  // 3. Start both workers
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
