import { LHTaskWorker, Workflow, buildPutStructDefRequest, createTaskWorker } from 'littlehorse-client'
import { StructDefCompatibilityType } from 'littlehorse-client/proto'
import { z } from 'zod'
import { loadConfig } from './config'
import { Address, ParkingTicketReport, Person } from './schemas'

const config = loadConfig()

function personToString(person: Person): string {
  return `${person.firstName} ${person.lastName}`
}

function addressToString(address: Address): string {
  return `${address.houseNumber} ${address.street}, ${address.city}, ${address.planet} ${address.zipCode}`
}

// Simulates a database lookup...
function lookupCarOwnerInDb(licensePlateNumber: string): Person {
  if (licensePlateNumber.startsWith('NOADDR')) {
    // Demonstrates nullable StructDef fields: address is intentionally unknown.
    return { firstName: 'Din', lastName: 'Djarin', homeAddress: null }
  }

  return {
    firstName: 'Obi-Wan',
    lastName: 'Kenobi',
    homeAddress: { houseNumber: 124, street: 'Sand Dune Lane', city: 'Anchorhead', planet: 'Tattooine', zipCode: 97412 },
  }
}

function getCarOwner(report: ParkingTicketReport): Person {
  return lookupCarOwnerInDb(report.licensePlateNumber)
}

function mailTicket(person: Person): string {
  // A null Struct field reaches the worker as undefined, so match both.
  if (person.homeAddress == null) {
    console.log(`No address for ${personToString(person)}. Routing ticket to manual follow-up queue.`)
    return `Ticket queued for manual follow-up for ${personToString(person)}`
  }

  console.log(`Sending mail to ${personToString(person)} at address ${addressToString(person.homeAddress)}`)
  return `Ticket sent to ${personToString(person)}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function registerStructDefs(worker: LHTaskWorker, ...schemas: z.ZodTypeAny[]) {
  for (const schema of schemas) {
    await worker.registerStructDef(buildPutStructDefRequest(schema, StructDefCompatibilityType.NO_SCHEMA_UPDATES))
  }
}

async function main() {
  const getCarOwnerWorker = createTaskWorker(getCarOwner, 'get-car-owner', config, {
    inputVars: { report: ParkingTicketReport },
    outputSchema: Person,
  })
  const mailTicketWorker = createTaskWorker(mailTicket, 'mail-ticket', config, {
    inputVars: { person: Person },
  })

  await registerStructDefs(getCarOwnerWorker, ParkingTicketReport, Address, Person)

  await ensureTaskDef(getCarOwnerWorker)
  await ensureTaskDef(mailTicketWorker)

  const wf = Workflow.newWorkflow('issue-parking-ticket', thread => {
    const ticketReport = thread.declareStruct('ticket-report', ParkingTicketReport).required()
    const carOwner = thread.declareStruct('car-owner', Person)

    carOwner.assign(thread.execute('get-car-owner', ticketReport))

    thread.execute('mail-ticket', carOwner)
  })
  await wf.registerWfSpec(config)

  await getCarOwnerWorker.start()
  await mailTicketWorker.start()
  console.log('ready: polling for get-car-owner and mail-ticket tasks')
  console.log(
    `run the workflow:  lhctl run issue-parking-ticket ticket-report '{"vehicleMake": "BARC", "vehicleModel": "Speeder", "licensePlateNumber": "1HGCM82633A004352"}'`
  )

  const shutdown = async () => {
    await getCarOwnerWorker.close()
    await mailTicketWorker.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
