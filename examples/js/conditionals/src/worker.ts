import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function taskA(): string {
  console.log('Executing task-a')
  return 'hello there A'
}

function taskB(): string {
  console.log('Executing task-b')
  return 'hello there B'
}

function taskC(): string {
  console.log('Executing task-c')
  return 'hello there C'
}

function taskD(): string {
  console.log('Executing task-d')
  return 'hello there D'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const workers = [
    createTaskWorker(taskA, 'task-a', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(taskB, 'task-b', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(taskC, 'task-c', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(taskD, 'task-d', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
  ]
  for (const w of workers) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('example-conditionals', thread => {
    const foo = thread.declareJsonObj('foo')

    thread.execute('task-a')

    thread
      .doIf(foo.jsonPath('$.bar').isGreaterThan(10), ifHandler => {
        ifHandler.execute('task-b')
      })
      .doElse(elseHandler => {
        elseHandler.execute('task-c')
      })

    thread.execute('task-d')
  })
  await wf.registerWfSpec(config)

  for (const w of workers) await w.start()
  console.log('ready: polling for task-a, task-b, task-c, task-d tasks')
  console.log(`run the workflow:  lhctl run example-conditionals foo '{"bar": 15}'`)

  const shutdown = async () => {
    await Promise.all(workers.map(w => w.close()))
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
