import { LHConfig } from '../LHConfig'
import { allocatePort, containerLogs, dockerAvailable, LH_IMAGE, startLhStandalone } from './container'

/**
 * Brings up the server for the integration run and creates its tenant.
 *
 * By default this starts a fresh, uniquely named lh-standalone container on a
 * free port, so a run never depends on a server left behind by an earlier one.
 * That isolation is not cosmetic: LittleHorse metadata is immutable and WfSpec
 * registration is eventually consistent, so a warm server can pass tests that
 * a cold one fails (this suite has done exactly that).
 *
 * Escape hatches:
 *  - LH_IT_HOST / LH_IT_PORT — use a server you manage; no container started.
 *  - LH_IT_KEEP=1            — leave the container running after the run.
 *  - LH_IT_IMAGE             — override the image under test.
 */
export default async function globalSetup(): Promise<void> {
  const external = process.env.LH_IT_HOST !== undefined || process.env.LH_IT_PORT !== undefined

  let host: string
  let port: number

  if (external) {
    host = process.env.LH_IT_HOST ?? 'localhost'
    port = Number(process.env.LH_IT_PORT ?? '2023')
    console.log(`\n[integration] using externally managed server ${host}:${port}`)
  } else {
    if (!dockerAvailable()) {
      throw new Error(
        'Docker is required for the integration suite but is not available.\n' +
          'Either start Docker, or point the suite at a server you manage:\n' +
          '  LH_IT_HOST=localhost LH_IT_PORT=2023 npm run test:integration'
      )
    }
    port = await allocatePort()
    const started = startLhStandalone(port)
    host = started.host
    process.env.LH_IT_CONTAINER = started.name
    console.log(`\n[integration] started container ${started.name} (${LH_IMAGE}) on ${host}:${port}`)
  }

  process.env.LH_IT_HOST = host
  process.env.LH_IT_PORT = String(port)

  const client = LHConfig.fromMap({ LHC_API_HOST: host, LHC_API_PORT: String(port) }).getClient()
  const startedAt = Date.now()
  const deadline = startedAt + 180_000

  for (;;) {
    try {
      await client.putTaskDef({ name: 'lh-sdkjs-it-probe', inputVars: [] })
      break
    } catch (err) {
      if (Date.now() > deadline) {
        const container = process.env.LH_IT_CONTAINER
        throw new Error(
          `LittleHorse server at ${host}:${port} never became ready within 180s.\n` +
            `Underlying error: ${(err as Error).message}` +
            (container ? `\nContainer logs:\n${containerLogs(container)}` : '')
        )
      }
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
  }
  console.log(`[integration] server ready in ${((Date.now() - startedAt) / 1000).toFixed(1)}s`)

  // One tenant for the whole run: metadata is immutable, so a fresh namespace
  // is what makes repeated runs independent. Creating tenants from more than
  // one test file in a run fails with "Tenant not allowed", hence one here.
  const tenantId = `sdkjs-it-${Date.now().toString(36)}-${Math.floor(Math.random() * 1e6).toString(36)}`
  await client.putTenant({ id: tenantId })
  process.env.LH_IT_TENANT = tenantId
  console.log(`[integration] tenant ${tenantId}`)
}
