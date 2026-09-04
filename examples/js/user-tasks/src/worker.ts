import { LHTaskWorker, WorkerContext, Workflow, createTaskWorker, userTaskSchema } from 'littlehorse-client'
import { z } from 'zod'
import { loadConfig } from './config'

const config = loadConfig()
const client = config.getClient()

function sendEmail(address: string, content: string, ctx: WorkerContext): void {
  if (ctx.getUserId() !== undefined) {
    console.log(`Received variable by ${ctx.getUserId()}`)
  } else if (ctx.getUserGroup() !== undefined) {
    console.log(`Received variable by ${ctx.getUserGroup()}`)
  }

  console.log(`\n\nSending email to ${address}`)
  console.log(`Content: ${content}`)
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef()
}

async function main() {
  const emailSender = createTaskWorker(sendEmail, 'send-email', config, {
    inputVars: { address: z.string(), content: z.string() },
  })
  await ensureTaskDef(emailSender)

  // The forms a human fills in, described with zod plus presentation metadata.
  await client.putUserTaskDef(
    userTaskSchema('it-request', {
      requestedItem: {
        schema: z.string(),
        displayName: 'Your Request',
        description: 'The item you are requesting.',
      },
      justification: {
        schema: z.string(),
        displayName: 'Request Justification',
        description: 'Why you need this request.',
      },
    }).compile()
  )
  await client.putUserTaskDef(
    userTaskSchema('approve-it-request', {
      isApproved: {
        schema: z.boolean(),
        displayName: 'Approved?',
        description: "Reply 'true' if this is an acceptable request.",
      },
    }).compile()
  )

  const wf = Workflow.newWorkflow('it-request', thread => {
    const userId = thread.declareStr('user-id')
    const itRequest = thread.declareJsonObj('it-request')
    const isApproved = thread.declareBool('is-approved')

    // Get the IT Request
    const formOutput = thread.assignUserTask('it-request', { userId, userGroup: 'testGroup' })
    thread.releaseToGroupOnDeadline(formOutput, 60)

    thread.handleException(formOutput, null, handler => {
      const email = 'test-ut-support@gmail.com'
      handler.execute('send-email', email, 'Task cancelled')
    })
    itRequest.assign(formOutput)

    // Have Finance approve the request
    const financeUserTaskOutput = thread.assignUserTask('approve-it-request', {
      userGroup: 'finance',
      notes: thread.format(
        'User {0} is requesting to buy item {1}.\nJustification: {2}',
        userId,
        itRequest.jsonPath('$.requestedItem'),
        itRequest.jsonPath('$.justification')
      ),
    })
    const financeTeamEmailBody = 'Hi finance team, you have a new assigned task'
    const financeTeamEmail = 'finance@gmail.com'
    thread.scheduleReminderTask(financeUserTaskOutput, 2, 'send-email', financeTeamEmail, financeTeamEmailBody)
    thread.reassignUserTask(financeUserTaskOutput, 'test-eduwer', null, 60)

    isApproved.assign(financeUserTaskOutput.jsonPath('$.isApproved'))

    thread
      .doIf(isApproved.isEqualTo(true), ifBody => {
        // Request approved!
        ifBody.execute(
          'send-email',
          userId,
          thread.format(
            'Dear {0}, your request for {1} has been approved!',
            userId,
            itRequest.jsonPath('$.requestedItem')
          )
        )
      })
      .doElse(elseBody => {
        // Request denied ):
        elseBody.execute(
          'send-email',
          userId,
          thread.format(
            'Dear {0}, your request for {1} has been denied.',
            userId,
            itRequest.jsonPath('$.requestedItem')
          )
        )
      })
  })
  await wf.registerWfSpec(config)

  await emailSender.start()
  console.log('ready: polling for send-email tasks')
  console.log('run the workflow:  lhctl run it-request user-id anakin')

  const shutdown = async () => {
    await emailSender.close()
    process.exit(0)
  }
  process.on('SIGINT', shutdown)
  process.on('SIGTERM', shutdown)
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
