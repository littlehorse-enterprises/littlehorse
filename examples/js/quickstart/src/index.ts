import { LHConfig, Workflow, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { LHErrorType } from 'littlehorse-client/proto'
import { z } from 'zod'

/**
 * Simulates verifying a customer's identity using their full name, email, and SSN.
 * In a real application this would call an external identity service.
 */
function verifyIdentity(fullName: string, email: string, ssn: number, ctx: WorkerContext): void {
  const msg = `[verify-identity] Verifying identity for ${fullName} (${email}) — WfRun ${ctx.getWfRunId()?.id}`
  ctx.log(msg)
  console.log(msg)
  console.log(`[verify-identity] Verification request submitted for ${email}`)
}

/**
 * Notifies the customer that their identity was verified successfully.
 */
function notifyCustomerVerified(fullName: string, email: string, ctx: WorkerContext): string {
  const msg = `[notify-customer-verified] Identity confirmed! Sending approval to ${fullName} <${email}> — WfRun ${ctx.getWfRunId()?.id}`
  ctx.log(msg)
  console.log(msg)
  return msg
}

/**
 * Notifies the customer that their identity could not be verified.
 */
function notifyCustomerNotVerified(fullName: string, email: string, ctx: WorkerContext): string {
  const msg = `[notify-customer-not-verified] Identity could not be confirmed. Sending rejection to ${fullName} <${email}> — WfRun ${ctx.getWfRunId()?.id}`
  ctx.log(msg)
  console.log(msg)
  return msg
}

async function main() {
  // Connect to the LH Server (defaults to localhost:2023)
  const config = LHConfig.from({})

  const verifyIdentityWorker = createTaskWorker(verifyIdentity, 'verify-identity', config, {
    inputVars: {
      'full-name': z.string(),
      email: z.string(),
      ssn: z.number().int(),
    },
  })

  const notifyVerifiedWorker = createTaskWorker(notifyCustomerVerified, 'notify-customer-verified', config, {
    inputVars: { 'full-name': z.string(), email: z.string() },
    outputSchema: z.string(),
  })

  const notifyNotVerifiedWorker = createTaskWorker(notifyCustomerNotVerified, 'notify-customer-not-verified', config, {
    inputVars: { 'full-name': z.string(), email: z.string() },
    outputSchema: z.string(),
  })

  // Register TaskDefs if they don't exist yet
  for (const worker of [verifyIdentityWorker, notifyVerifiedWorker, notifyNotVerifiedWorker]) {
    if (!(await worker.doesTaskDefExist())) {
      console.log(`TaskDef "${worker.getTaskDefName()}" not found, registering...`)
      await worker.registerTaskDef()
    }
  }

  const wf = Workflow.newWorkflow('quickstart', thread => {
    const fullName = thread.declareStr('full-name').searchable().required()
    const email = thread.declareStr('email').searchable().required()
    const ssn = thread.declareInt('ssn').masked().required()
    const identityVerified = thread.declareBool('identity-verified').searchable()

    thread.execute('verify-identity', fullName, email, ssn).withRetries(3)

    const result = thread
      .waitForEvent('identity-verified')
      .timeout(300)
      .withCorrelationId(email)
      .registeredAs(z.boolean())

    thread.handleError(result, LHErrorType.TIMEOUT, handler => {
      handler.execute('notify-customer-not-verified', fullName, email)
      handler.fail('customer-not-verified', 'Unable to verify customer identity in time.')
    })

    identityVerified.assign(result)
    thread
      .doIf(identityVerified.isEqualTo(true), ifBody => {
        ifBody.execute('notify-customer-verified', fullName, email)
      })
      .doElse(elseBody => {
        elseBody.execute('notify-customer-not-verified', fullName, email)
      })
  })
  await wf.registerWfSpec(config)

  // Start polling for tasks
  await verifyIdentityWorker.start()
  await notifyVerifiedWorker.start()
  await notifyNotVerifiedWorker.start()

  console.log('Quickstart task workers running. Press Ctrl+C to stop.')

  // Graceful shutdown on Ctrl+C
  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await Promise.all([verifyIdentityWorker.close(), notifyVerifiedWorker.close(), notifyNotVerifiedWorker.close()])
    process.exit(0)
  })
}

main().catch(err => {
  console.error(err)
  process.exit(1)
})
