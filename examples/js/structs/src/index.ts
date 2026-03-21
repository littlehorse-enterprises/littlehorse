import {
  LHConfig,
  Workflow,
  createTaskWorker,
  WorkerContext,
  buildPutStructDefRequest,
  getStructDependencies,
  getStructName,
} from 'littlehorse-client'
import { Address, Person, ParkingTicketReport } from './schemas.js'
import type { Person as PersonType, ParkingTicketReport as ParkingTicketReportType } from './schemas.js'

function issueParkingTicketWorkflow() {
  return Workflow.newWorkflow('issue-parking-ticket', thread => {
    const carInput = thread.declareStruct('car-input', 'parking-ticket-report')
    const ownerLookup = thread.execute('get-car-owner', carInput)
    const carOwner = thread.declareStruct('car-owner', 'person')
    carOwner.assign(ownerLookup)
    thread.execute('mail-ticket', carOwner)
  })
}

function getCarOwner(report: ParkingTicketReportType, _ctx: WorkerContext): PersonType {
  console.log(`[get-car-owner] Looking up owner for plate: ${report.licensePlateNumber}`)
  return {
    firstName: 'Obi-Wan',
    lastName: 'Kenobi',
    homeAddress: { houseNumber: 124, street: 'Sand Dune Lane', city: 'Anchorhead', planet: 'Tattooine', zipCode: 97412 },
  }
}

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

  console.log('Registering TaskDef "get-car-owner"...')
  await getCarOwnerWorker.registerTaskDef()
  console.log('Registering TaskDef "mail-ticket"...')
  await mailTicketWorker.registerTaskDef()

  console.log('Registering WfSpec "issue-parking-ticket"...')
  await Workflow.registerWfSpec(issueParkingTicketWorkflow(), config.getClient())

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
