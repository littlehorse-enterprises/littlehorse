import { LHConfig, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

/**
 * This is the task function that gets executed every time the "greet" task
 * is scheduled. The LH Server sends input variables as positional arguments;
 * the WorkerContext is appended as the last argument automatically.
 */
async function greet(name: string, ctx: WorkerContext): Promise<string> {
  const msg = `Hello, ${name}! (WfRun ${ctx.getWfRunId()?.id})`
  ctx.log(msg)
  console.log(msg)
  return msg
}

async function main() {
  // Connect to the LH Server (defaults to localhost:2023)
  const config = LHConfig.from({})

  const worker = createTaskWorker(greet, 'greet', config, {
    inputVars: { name: z.string() },
  })

  // Register the TaskDef if it doesn't exist yet
  if (!(await worker.doesTaskDefExist())) {
    console.log('TaskDef "greet" not found, registering...')
    await worker.registerTaskDef()
  }

  // Start polling for tasks
  await worker.start()

  // Graceful shutdown on Ctrl+C
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
