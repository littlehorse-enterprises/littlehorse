import { LHTaskWorker, Workflow, createTaskWorker, toTypeDefinition } from 'littlehorse-client'
import { Timestamp, VariableType } from 'littlehorse-client/proto'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

const STR = toTypeDefinition(VariableType.STR)
const TIMESTAMP = toTypeDefinition(VariableType.TIMESTAMP)
const JSON_OBJ = toTypeDefinition(VariableType.JSON_OBJ)

// The timestamp representations available in JS (Java's Book carries Date,
// Instant, protobuf Timestamp, java.sql.Timestamp, and LocalDateTime).
interface Book {
  name: string
  publishDate: Date
  publishInstant: string
  publishTimestamp: Timestamp
  publishSqlTimestamp: number
  publishLocalDateTime: string
}

function getDate(): Date {
  return new Date()
}

function publishBook(name: string, instant: Date): Book {
  console.log(`Publishing book ${name} at date ${instant.toISOString()}`)
  return {
    name,
    publishDate: instant,
    publishInstant: instant.toISOString(),
    publishTimestamp: Timestamp.fromDate(instant),
    publishSqlTimestamp: instant.getTime(),
    publishLocalDateTime: instant.toLocaleString(),
  }
}

function printBookDetails(publishedBook: Book, instant: Date): string {
  console.log(
    `${publishedBook.name} was published in ${publishedBook.publishDate}, this information is printed at ${instant.toISOString()}`
  )
  return ''
}

// createTaskWorker's zod mapping has no TIMESTAMP type, so the TaskDefs are
// registered through the client with the same signatures Java derives from
// the worker methods.
async function ensureTaskDef(worker: LHTaskWorker, request: Parameters<typeof client.putTaskDef>[0]) {
  if (!(await worker.doesTaskDefExist())) await client.putTaskDef(request)
}

async function main() {
  const currentDate = createTaskWorker(getDate, 'get-current-date', config, { inputVars: {} })
  const publish = createTaskWorker(publishBook, 'publish-book', config, {
    inputVars: { name: z.string(), instant: z.date() },
  })
  const printDetails = createTaskWorker(printBookDetails, 'print-book-details', config, {
    inputVars: { publishedBook: z.object({}), instant: z.date() },
  })

  await ensureTaskDef(currentDate, {
    name: 'get-current-date',
    inputVars: [],
    returnType: { returnType: TIMESTAMP },
  })
  await ensureTaskDef(publish, {
    name: 'publish-book',
    inputVars: [
      { name: 'name', typeDef: STR },
      { name: 'instant', typeDef: TIMESTAMP },
    ],
    returnType: { returnType: JSON_OBJ },
  })
  await ensureTaskDef(printDetails, {
    name: 'print-book-details',
    inputVars: [
      { name: 'publishedBook', typeDef: JSON_OBJ },
      { name: 'instant', typeDef: TIMESTAMP },
    ],
    returnType: { returnType: STR },
  })

  const wf = Workflow.newWorkflow('example-timestamp', thread => {
    const publishDate = thread.declareTimestamp('publish-date').withDefault(new Date('1997-06-26T12:12:12Z'))
    const bookName = thread.declareStr('book-name').withDefault("Harry Potter and the Philosopher's Stone")
    const publishBook = thread.execute('publish-book', bookName, publishDate)
    const currentDate = thread.execute('get-current-date')
    thread.execute('print-book-details', publishBook, currentDate)
  })
  await wf.registerWfSpec(config)

  for (const w of [currentDate, publish, printDetails]) await w.start()
  console.log('ready: polling for get-current-date, publish-book, print-book-details tasks')
  console.log('run the workflow:  lhctl run example-timestamp book-name "My Book" publish-date 1997-06-26T12:12:12Z')

  const shutdown = async () => {
    await Promise.all([currentDate.close(), publish.close(), printDetails.close()])
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
