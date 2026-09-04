import { LHTaskWorker, Workflow, buildPutStructDefRequest, createTaskWorker } from 'littlehorse-client'
import { StructDefCompatibilityType } from 'littlehorse-client/proto'
import { z } from 'zod'
import { loadConfig } from './config'
import { Address, Person } from './schemas'

const config = loadConfig()

function addressToString(address: Address): string {
  return `${address.street}, ${address.city}, ${address.state} ${address.zip}`
}

function personToString(person: Person): string {
  return `${person.name} <${person.email}> at ${addressToString(person.address)}`
}

// Simulates looking up an address by name. In a real application this would
// call a database or external service and return a JSON object.
function fetchAddress(name: string): Address {
  console.log(`Looking up address for ${name}`)
  return { street: '124 Sand Dune Lane', city: 'Anchorhead', state: 'Tattooine', zip: 97412 }
}

function savePerson(person: Person): string {
  console.log(`Saving person record: ${personToString(person)}`)
  return `Saved ${personToString(person)}`
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
  const fetchAddressWorker = createTaskWorker(fetchAddress, 'fetch-address', config, {
    inputVars: { name: z.string() },
    outputSchema: Address,
  })
  const savePersonWorker = createTaskWorker(savePerson, 'save-person', config, {
    inputVars: { person: Person },
  })

  // address must be registered before person: person references it by name.
  await registerStructDefs(fetchAddressWorker, Address, Person)

  await ensureTaskDef(fetchAddressWorker)
  await ensureTaskDef(savePersonWorker)

  const wf = Workflow.newWorkflow('assemble-person', thread => {
    const name = thread.declareStr('name').required()
    const email = thread.declareStr('email').required()

    const personRecord = thread.declareStruct('person-record', Person)

    const addressOutput = thread.execute('fetch-address', name)

    // The key feature: assemble a Struct inside the WfSpec with buildStruct +
    // buildInlineStruct instead of relying on a task to return a complete one.
    const personStruct = thread
      .buildStruct('person')
      .put('name', name)
      .put('email', email)
      .put(
        'address',
        thread
          .buildInlineStruct()
          .put('street', addressOutput.get('street'))
          .put('city', addressOutput.get('city'))
          .put('state', addressOutput.get('state'))
          .put('zip', addressOutput.get('zip'))
      )

    personRecord.assign(personStruct)

    thread.execute('save-person', personRecord)
  })
  await wf.registerWfSpec(config)

  await fetchAddressWorker.start()
  await savePersonWorker.start()
  console.log('ready: polling for fetch-address and save-person tasks')
  console.log('run the workflow:  lhctl run assemble-person name Obi-Wan email obi-wan@jedi.org')

  const shutdown = async () => {
    await fetchAddressWorker.close()
    await savePersonWorker.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
