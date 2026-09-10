import { LHTaskWorker, WorkerContext, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

// The context is always the last parameter in the signature
function task(requestTime: number, ctx: WorkerContext): void {
  const end = Date.now()
  const lag = end - requestTime

  console.log(`Epochs: start ${requestTime} end ${end}`)
  console.log(
    `Started ${new Date(requestTime).toISOString()}, Finished ${new Date(end).toISOString()}. Lag in millis: ${lag}`
  )
  console.log(
    `Wf run id '${ctx.getWfRunId()?.id}'. Task global id '${ctx.getTaskRunId()?.taskGuid}'. Attempt number '${ctx.getAttemptNumber()}'`
  )
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const worker = createTaskWorker(task, 'task', config, {
    inputVars: { requestTime: z.number().int() },
  })
  await ensureTaskDef(worker)

  const wf = Workflow.newWorkflow('example-worker-context', thread => {
    const theName = thread.declareInt('request-time')
    thread.execute('task', theName)
  })
  await wf.registerWfSpec(config)

  await worker.start()
  console.log('ready: polling for task tasks')
  console.log('run the workflow:  lhctl run example-worker-context request-time $(date +%s%3N)')

  const shutdown = async () => {
    await worker.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
