import { z } from 'zod'
import { varValToObj } from '../common'
import { LHConfig } from '../LHConfig'
import type { LHPublicClient } from '../client'
import { LHStatus } from '../proto/common_enums'
import { VariableValue } from '../proto/type_definition'
import { WfRun } from '../proto/wf_run'
import { WfRunId } from '../proto/object_id'
import { createTaskWorker, LHTaskWorker, TaskFunction } from '../worker'
import { Workflow } from '../wfsdk'
import { zodToTypeDef } from '../worker/zodSchema'

/**
 * Integration harness: tier 2 of PARITY_PLAN.md.
 *
 * These tests run against a REAL LittleHorse server, unlike the fake server
 * used by the worker unit tests. They are the only thing that proves the
 * server actually accepts what the JS SDK produces and executes it correctly.
 *
 * Just run `npm run test:integration` — globalSetup starts a fresh container
 * per run and globalTeardown removes it. See container.ts for the escape
 * hatches (LH_IT_HOST/LH_IT_PORT, LH_IT_KEEP, LH_IT_IMAGE).
 */

export const LH_HOST = process.env.LH_IT_HOST ?? 'localhost'
export const LH_PORT = process.env.LH_IT_PORT ?? '2023'

export function integrationConfig(tenantId?: string): LHConfig {
  return LHConfig.fromMap({
    LHC_API_HOST: LH_HOST,
    LHC_API_PORT: LH_PORT,
    ...(tenantId !== undefined && { LHC_TENANT_ID: tenantId }),
  })
}

/**
 * The tenant for this run, created once by globalSetup.ts. Every test file
 * shares it, which keeps the run hermetic without creating tenants from
 * multiple files (which the server rejects with "Tenant not allowed").
 */
export function integrationTenant(): string {
  const tenantId = process.env.LH_IT_TENANT
  if (!tenantId) {
    throw new Error('LH_IT_TENANT is not set — run via `npm run test:integration` so globalSetup creates a tenant.')
  }
  return tenantId
}

/** A config bound to this run's tenant. */
export async function tenantConfig(): Promise<LHConfig> {
  return integrationConfig(integrationTenant())
}

/** A client bound to this run's tenant. */
export async function tenantClient(): Promise<LHPublicClient> {
  return (await tenantConfig()).getClient()
}

/** Returns true if a LittleHorse server is reachable and serving requests. */
export async function isServerReachable(timeoutMs = 5000): Promise<boolean> {
  const client = integrationConfig().getClient()
  const deadline = Date.now() + timeoutMs
  while (Date.now() < deadline) {
    try {
      await client.putTaskDef({ name: 'lh-sdkjs-it-probe', inputVars: [] })
      return true
    } catch {
      await sleep(500)
    }
  }
  return false
}

export function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * Fails the suite with actionable instructions when no server is running.
 * Deliberately a hard failure rather than a silent skip: an integration suite
 * that quietly passes without a server is worse than no suite at all.
 */
export async function requireServer(timeoutMs = 60000): Promise<void> {
  if (await isServerReachable(timeoutMs)) return
  throw new Error(
    `No LittleHorse server at ${LH_HOST}:${LH_PORT}.\n` +
      `globalSetup normally starts one and waits for it, so reaching this means the\n` +
      `server died mid-run, or the suite was invoked without its config. Run it with:\n` +
      `  npm run test:integration`
  )
}

/** Unique suffix so repeated runs don't collide on metadata names. */
export function uniqueName(prefix: string): string {
  return `${prefix}-${Date.now().toString(36)}-${Math.floor(Math.random() * 1e6).toString(36)}`
}

/**
 * Registers the TaskDefs a workflow needs. The server rejects a WfSpec that
 * references an unknown TaskDef, so this must happen before registerWfSpec.
 */
export async function registerRequiredTaskDefs(
  client: LHPublicClient,
  workflow: Workflow,
  inputVarsByTask: Record<string, Record<string, z.ZodTypeAny>> = {}
): Promise<void> {
  for (const taskDefName of workflow.getRequiredTaskDefNames()) {
    const inputVars = inputVarsByTask[taskDefName] ?? {}
    await client.putTaskDef({
      name: taskDefName,
      inputVars: Object.entries(inputVars).map(([name, schema]) => ({
        name,
        typeDef: zodToTypeDef(schema),
      })),
    })
  }
}

/**
 * Waits until a registered WfSpec is visible to the server.
 *
 * Registration is asynchronous: `putWfSpec` returns before the spec is
 * queryable, so an immediate `runWf` can fail with "Couldn't find specified
 * WfSpec". It is reliably fast on a warm server, which is why this only shows
 * up against a cold one — and why no offline test could have found it.
 */
export async function awaitWfSpecReady(client: LHPublicClient, name: string, timeoutMs = 30000): Promise<void> {
  const deadline = Date.now() + timeoutMs
  for (;;) {
    try {
      await client.getLatestWfSpec({ name })
      return
    } catch (err) {
      if (Date.now() > deadline) {
        throw new Error(`WfSpec ${name} never became visible within ${timeoutMs}ms: ${(err as Error).message}`)
      }
      await sleep(100)
    }
  }
}

export interface RunningWorker {
  worker: LHTaskWorker
  close(): Promise<void>
}

/** Starts a worker for one TaskDef, bound to this file's tenant. */
export async function startWorker(
  taskDefName: string,
  taskFunction: TaskFunction,
  inputVars: Record<string, z.ZodTypeAny> = {}
): Promise<RunningWorker> {
  const worker = createTaskWorker(taskFunction, taskDefName, await tenantConfig(), {
    inputVars,
    heartbeatIntervalMs: 1000,
  })
  await worker.start()
  return { worker, close: () => worker.close() }
}

/** Polls until the WfRun reaches a terminal status, then returns it. */
export async function waitForWfRun(client: LHPublicClient, wfRunId: WfRunId, timeoutMs = 30000): Promise<WfRun> {
  const deadline = Date.now() + timeoutMs
  let last: WfRun | undefined
  while (Date.now() < deadline) {
    last = await client.getWfRun(wfRunId)
    if (isTerminal(last.status)) return last
    await sleep(200)
  }
  throw new Error(
    `WfRun ${wfRunId.id} did not finish within ${timeoutMs}ms (last status: ${last ? LHStatus[last.status] : 'unknown'})`
  )
}

export function isTerminal(status: LHStatus): boolean {
  return status === LHStatus.COMPLETED || status === LHStatus.ERROR || status === LHStatus.EXCEPTION
}

/** Reads a variable's value from a finished (or running) WfRun. */
export async function getVariable(
  client: LHPublicClient,
  wfRunId: WfRunId,
  name: string,
  threadRunNumber = 0
): Promise<VariableValue | undefined> {
  const variable = await client.getVariable({ wfRunId, threadRunNumber, name })
  return variable.value
}

/**
 * Unwraps a VariableValue into a plain JS value for assertions.
 *
 * Delegates to the SDK's own deserializer rather than reimplementing it, so
 * assertions see exactly what a user's task function would receive — a private
 * copy here would silently diverge (it did: it had no STRUCT case).
 */
export function unwrap(value: VariableValue | undefined): unknown {
  if (!value?.value) return undefined
  return varValToObj(value)
}
