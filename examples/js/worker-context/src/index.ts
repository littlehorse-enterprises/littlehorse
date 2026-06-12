import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

function workerContextWorkflow() {
  return Workflow.newWorkflow('example-worker-context', thread => {
    const requestTime = thread.declareInt('request-time')
    thread.execute('task', requestTime)
  })
}

async function task(requestTime: number, ctx: WorkerContext): Promise<void> {
  const end = Date.now()
  const lag = end - requestTime
  ctx.log(`Lag ms: ${lag}`)
  console.log(
    `[task] requestTime=${requestTime} end=${end} wfRun=${ctx.getWfRunId()?.id} attempt=${ctx.getAttemptNumber()}`
  )
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const worker = createTaskWorker(task, 'task', config, {
    inputVars: { requestTime: z.number().int() },
  })

  if (!(await worker.doesTaskDefExist())) {
    await worker.registerTaskDef()
  }

  console.log('Registering WfSpec "example-worker-context"...')
  await Workflow.registerWfSpec(workerContextWorkflow(), client)

  await worker.start()

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await worker.close()
    process.exit(0)
  })
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
