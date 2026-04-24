import { LHConfig, createTaskWorker, WorkerContext, Workflow } from 'littlehorse-client'
import { z } from 'zod'

function basicWorkflow() {
  return Workflow.newWorkflow('example-basic', thread => {
    const inputName = thread.declareStr('input-name')
    thread.execute('greet', inputName)
  })
}

async function greet(name: string, ctx: WorkerContext): Promise<string> {
  const msg = `Hello, ${name}! (WfRun ${ctx.getWfRunId()?.id})`
  ctx.log(msg)
  console.log(msg)
  return msg
}

async function main() {
  const config = LHConfig.from({})
  const client = config.getClient()

  const worker = createTaskWorker(greet, 'greet', config, {
    inputVars: { name: z.string() },
  })

  if (!(await worker.doesTaskDefExist())) {
    console.log('TaskDef "greet" not found, registering...')
    await worker.registerTaskDef()
  }

  console.log('Registering WfSpec "example-basic"...')
  await Workflow.registerWfSpec(basicWorkflow(), client)

  await worker.start()

  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await worker.close()
    process.exit(0)
  })
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
