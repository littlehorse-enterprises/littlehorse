import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function exec(n: number): void {
  console.log(`Execution n = ${n}`)
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const executionNumber = createTaskWorker(exec, 'execution-number', config, {
    inputVars: { n: z.number().int() },
  })
  await ensureTaskDef(executionNumber)

  const wf = Workflow.newWorkflow('example-run-wf', thread => {
    const n = thread.declareInt('n')
    thread.execute('execution-number', n)
  })
  await wf.registerWfSpec(config)

  await executionNumber.start()
  console.log('ready: polling for execution-number tasks')
  console.log('run the workflow:  lhctl run example-run-wf n 1')

  const shutdown = async () => {
    await executionNumber.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
