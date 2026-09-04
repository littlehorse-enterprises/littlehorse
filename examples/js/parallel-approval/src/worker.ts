import {
  LHTaskWorker,
  ThreadFunc,
  WfRunVariable,
  Workflow,
  createTaskWorker,
  spawnedThreadsOf,
} from 'littlehorse-client'
import { Comparator } from 'littlehorse-client/proto'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

function calculateNextNotification(): number {
  console.log('Executing calculate-next-notification')
  return Date.now() + 20 * 1000
}

function reminderTask(): string {
  console.log('\n\n\n\n******\nreminder-task!!!\n******\n\n\n')
  return 'I just sent a reminder!'
}

function handler(): string {
  console.log('Ok, handler was called by exception handler')
  return 'Ok?'
}

function waitForPerson1(person1Approved: WfRunVariable): ThreadFunc {
  return approvalThread => {
    const jsonVariable = approvalThread.declareJsonObj('person-1-response')
    jsonVariable.assign(approvalThread.waitForEvent('person-1-approves'))
    approvalThread
      .doIf(approvalThread.condition(jsonVariable.jsonPath('$.approval'), Comparator.EQUALS, true), ifHandler => {
        person1Approved.assign(true)
      })
      .doElse(elseHandler => {
        approvalThread.fail('denied-by-user', 'message here')
      })
  }
}

function waitForPerson2(person2Approved: WfRunVariable): ThreadFunc {
  return approvalThread => {
    const jsonVariable = approvalThread.declareJsonObj('person-2-response')
    jsonVariable.assign(approvalThread.waitForEvent('person-2-approves'))
    approvalThread
      .doIf(approvalThread.condition(jsonVariable.jsonPath('$.approval'), Comparator.EQUALS, true), ifHandler => {
        person2Approved.assign(true)
      })
      .doElse(elseHandler => {
        approvalThread.fail('denied-by-user', 'message here')
      })
  }
}

function waitForPerson3(person3Approved: WfRunVariable): ThreadFunc {
  return approvalThread => {
    const jsonVariable = approvalThread.declareJsonObj('person-3-response')
    jsonVariable.assign(approvalThread.waitForEvent('person-3-approves'))
    approvalThread
      .doIf(approvalThread.condition(jsonVariable.jsonPath('$.approval'), Comparator.EQUALS, true), ifHandler => {
        person3Approved.assign(true)
      })
      .doElse(elseHandler => {
        approvalThread.fail('denied-by-user', 'message here')
      })
  }
}

function sendReminders(allApproved: WfRunVariable): ThreadFunc {
  return reminderThread => {
    const nextReminderTime = reminderThread.declareInt('next-reminder')

    // Calculate next time to send notification
    nextReminderTime.assign(reminderThread.execute('calculate-next-notification'))

    reminderThread.sleepUntil(nextReminderTime)

    // So long as all things haven't been approved yet, continue to send reminders.
    reminderThread.doWhile(reminderThread.condition(allApproved, Comparator.EQUALS, false), loop => {
      reminderThread.execute('reminder-task')

      // Calculate next reminder
      nextReminderTime.assign(reminderThread.execute('calculate-next-notification'))

      // Wait until next reminder
      reminderThread.sleepUntil(nextReminderTime)
    })
  }
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const workers = [
    createTaskWorker(calculateNextNotification, 'calculate-next-notification', config, { inputVars: {} }),
    createTaskWorker(reminderTask, 'reminder-task', config, { inputVars: {} }),
    createTaskWorker(handler, 'exc-handler', config, { inputVars: {} }),
  ]
  for (const worker of workers) await ensureTaskDef(worker)

  const workflow = Workflow.newWorkflow('parallel-approval', wf => {
    const person1Approved = wf.declareBool('person-1-approved')
    const person2Approved = wf.declareBool('person-2-approved')
    const person3Approved = wf.declareBool('person-3-approved')
    const allApproved = wf.declareBool('all-approved')

    // Variables are initialized to NULL. Need to set to a real value.
    allApproved.assign(false)
    person1Approved.assign(false)
    person2Approved.assign(false)
    person3Approved.assign(false)

    // Kick off the reminder workflow
    wf.spawnThread(sendReminders(allApproved), 'send-reminders')

    // Wait for all users to approve the transaction
    const p1Thread = wf.spawnThread(waitForPerson1(person1Approved), 'person-1')
    const p2Thread = wf.spawnThread(waitForPerson2(person2Approved), 'person-2')
    const p3Thread = wf.spawnThread(waitForPerson3(person3Approved), 'person-3')

    const nodeOutput = wf.waitForThreads(spawnedThreadsOf(p1Thread, p2Thread, p3Thread))

    wf.handleException(nodeOutput, 'denied-by-user', xnHandler => {
      // HANDLE FAILED APPROVALS HERE.
      // If you want, you can execute additional business logic.
      xnHandler.fail('denied-by-user', 'The workflow was not approved!')
    })

    // Tell the reminder workflow to stop
    allApproved.assign(true)
  })

  // The ExternalEventDefs must exist before the WfSpec may reference them.
  for (const externalEventName of workflow.getRequiredExternalEventDefNames()) {
    await client.putExternalEventDef({ name: externalEventName })
  }
  await workflow.registerWfSpec(config)

  for (const worker of workers) await worker.start()
  console.log('ready: polling for calculate-next-notification, reminder-task and exc-handler tasks')
  console.log('run the workflow:  lhctl run parallel-approval')

  const shutdown = async () => {
    for (const worker of workers) await worker.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
