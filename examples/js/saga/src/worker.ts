import { randomUUID } from 'crypto'
import { LHTaskWorker, Workflow, createTaskWorker, spawnedThreadsOf } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function bookFlight(): string {
  const confirmationNumber = randomUUID()
  console.log(`Running book-flight. Confirmation number ${confirmationNumber}`)
  return confirmationNumber
}

function cancelFlight(confirmationNumber: string): void {
  console.log(`Cancelling the fake flight with confirmation: ${confirmationNumber}`)
}

function bookHotel(): string {
  console.log('Running book-hotel')
  if (Math.random() < 0.5) {
    console.error('Error when booking hotel')
    throw new Error('Yikes, hotel failed')
  }
  const confirmationNumber = randomUUID()
  console.log(`Hotel successfully booked. Confirmation ${confirmationNumber}`)
  return confirmationNumber
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const flightBooker = createTaskWorker(bookFlight, 'book-flight', config, {
    inputVars: {},
    outputSchema: z.string(),
  })
  const flightCanceller = createTaskWorker(cancelFlight, 'cancel-flight', config, {
    inputVars: { confirmationNumber: z.string() },
  })
  const hotelBooker = createTaskWorker(bookHotel, 'book-hotel', config, {
    inputVars: {},
    outputSchema: z.string(),
  })
  for (const w of [flightBooker, flightCanceller, hotelBooker]) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-saga', thread => {
    const flightConfirmationNumber = thread.declareStr('flight-confirmation-number')
    const hotelConfirmationNumber = thread.declareStr('hotel-confirmation-number')

    const sagaThread = thread.spawnThread(bookThread => {
      const bookFlightOutput = bookThread.execute('book-flight')
      flightConfirmationNumber.assign(bookFlightOutput)

      const bookHotelOutput = bookThread.execute('book-hotel')
      hotelConfirmationNumber.assign(bookHotelOutput)
    }, 'example-saga')

    // If there is any failure (a business EXCEPTION or a technical ERROR such as the
    // TASK_FAILURE thrown by book-hotel), we abort by compensating the flight booking.
    const waitForThread = thread.waitForThreads(spawnedThreadsOf(sagaThread))

    thread.handleAnyFailure(waitForThread, abortThread => {
      abortThread.execute('cancel-flight', flightConfirmationNumber)
    })
  })
  await wf.registerWfSpec(config)

  for (const w of [flightBooker, flightCanceller, hotelBooker]) await w.start()
  console.log('ready: polling for book-flight, cancel-flight, book-hotel tasks')
  console.log('run the workflow:  lhctl run example-saga')

  const shutdown = async () => {
    await Promise.all([flightBooker.close(), flightCanceller.close(), hotelBooker.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
