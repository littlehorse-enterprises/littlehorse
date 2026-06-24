import { LHConfig, createTaskWorker, WorkerContext } from 'littlehorse-client'
import { z } from 'zod'

/**
 * Simulates verifying a customer's identity using their full name, email, and SSN.
 * In a real application this would call an external identity service.
 */
function verifyIdentity(
  fullName: string,
  email: string,
  ssn: number,
  ctx: WorkerContext
): void {
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
    inputVars: { 'full-name': z.string(), email: z.string(), ssn: z.number().int() },
  })

  const notifyVerifiedWorker = createTaskWorker(notifyCustomerVerified, 'notify-customer-verified', config, {
    inputVars: { 'full-name': z.string(), email: z.string() },
  })

  const notifyNotVerifiedWorker = createTaskWorker(notifyCustomerNotVerified, 'notify-customer-not-verified', config, {
    inputVars: { 'full-name': z.string(), email: z.string() },
  })

  // Register TaskDefs if they don't exist yet
  for (const worker of [verifyIdentityWorker, notifyVerifiedWorker, notifyNotVerifiedWorker]) {
    if (!(await worker.doesTaskDefExist())) {
      console.log(`TaskDef "${worker.getTaskDefName()}" not found, registering...`)
      await worker.registerTaskDef()
    }
  }

  // Start polling for tasks
  await verifyIdentityWorker.start()
  await notifyVerifiedWorker.start()
  await notifyNotVerifiedWorker.start()

  console.log('Quickstart task workers running. Press Ctrl+C to stop.')

  // Graceful shutdown on Ctrl+C
  process.on('SIGINT', async () => {
    console.log('\nShutting down...')
    await Promise.all([
      verifyIdentityWorker.close(),
      notifyVerifiedWorker.close(),
      notifyNotVerifiedWorker.close(),
    ])
    process.exit(0)
  })
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
