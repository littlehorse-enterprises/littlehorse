import { LHTaskWorker, WorkerContext, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

// The WorkerContext enables checkpointing: each executeAndCheckpoint block
// runs once across all attempts of the TaskRun, so a retry replays the stored
// result instead of repeating the side effect.
async function greeting(name: string, context: WorkerContext): Promise<string> {
  const attemptNumber = context.getAttemptNumber()
  console.log(`Hello from task worker on attempt ${attemptNumber} before the checkpoint`)

  let result = await context.executeAndCheckpoint(checkpointContext => {
    checkpointContext.log('this is a checkpoint log')
    console.log(`Hello from task worker on attempt ${attemptNumber} in the first checkpoint`)
    return `hello ${name} from first checkpoint`
  })

  console.log('Hello from after the first checkpoint')

  if (attemptNumber === 0) {
    throw new Error('Throwing a failure in the second checkpoint to show how the checkpoint works')
  }

  result += await context.executeAndCheckpoint(context2 => {
    console.log('Hi from inside the second checkpoint')
    return ' and the second checkpoint'
  })

  console.log(`Hi from after the checkpoints on attemptNumber ${attemptNumber}`)

  return result + ' and after the second checkpoint'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const greet = createTaskWorker(greeting, 'greet', config, {
    inputVars: { name: z.string() },
    outputSchema: z.string(),
  })
  await ensureTaskDef(greet)

  const wf = Workflow.newWorkflow('example-checkpointed-tasks', thread => {
    const theName = thread.declareStr('input-name').searchable()
    thread.execute('greet', theName).withRetries(2)
  })
  await wf.registerWfSpec(config)

  await greet.start()
  console.log('ready: polling for greet tasks')
  console.log('run the workflow:  lhctl run example-checkpointed-tasks input-name "Qui-Gon Jinn"')

  const shutdown = async () => {
    await greet.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
