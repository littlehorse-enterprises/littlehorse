import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function task1(): string {
  return 'task-1'
}

function task2(): string {
  return 'task-2'
}

function task3(): string {
  return 'task-3'
}

function task4(): string {
  return 'task-4'
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const workers = [
    createTaskWorker(task1, 'task-1', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(task2, 'task-2', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(task3, 'task-3', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
    createTaskWorker(task4, 'task-4', config, {
      inputVars: {},
      outputSchema: z.string(),
    }),
  ]
  for (const w of workers) await ensureTaskDef(w)

  const wf = Workflow.newWorkflow('hundred-tasks', thread => {
    for (let i = 0; i < 25; i++) {
      thread.execute('task-1')
      thread.execute('task-2')
      thread.execute('task-3')
      thread.execute('task-4')
    }
  })
  await wf.registerWfSpec(config)

  for (const w of workers) await w.start()
  console.log('ready: polling for task-1, task-2, task-3, task-4 tasks')
  console.log('run the workflow:  lhctl run hundred-tasks')

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
