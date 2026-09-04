import { LHTaskWorker, Workflow, createTaskWorker } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()

function taskExecutor(taskInput: string): string {
  console.log(`ok, executing task with ${taskInput}`)
  return `Executed task with input: ${taskInput}`
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const executor = createTaskWorker(taskExecutor, 'task-executor', config, {
    inputVars: { taskInput: z.string() },
  })
  await ensureTaskDef(executor)

  const wf = Workflow.newWorkflow('spawn-parallel-threads-from-json-arr-variable', thread => {
    const approvalChain = thread.declareJsonObj('approval-chain')
    const spawnedThreads = thread.spawnThreadForEach(
      approvalChain.jsonPath('$.approvals'),
      'spawn-threads',
      innerThread => {
        // It is mandatory to declare the INPUT variable at the moment.
        innerThread.declareInt('not-used-variable')
        const inputVariable = innerThread.declareJsonObj('INPUT')
        innerThread.execute('task-executor', inputVariable.jsonPath('$.user'))
      },
      { 'not-used-variable': 1234 }
    )
    thread.waitForThreads(spawnedThreads)

    thread.execute('task-executor', approvalChain.jsonPath('$.description'))
  })
  await wf.registerWfSpec(config)

  await executor.start()
  console.log('ready: polling for task-executor tasks')
  console.log(
    `run the workflow:  lhctl run spawn-parallel-threads-from-json-arr-variable approval-chain '{"description": "demo for approvals", "approvals":  [{"user": "yoda"}, {"user": "chewbacca"}, {"user": "anakin"}]}'`
  )

  const shutdown = async () => {
    await executor.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
