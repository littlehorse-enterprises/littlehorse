import { LHConfig } from '../LHConfig'

/**
 * Creates one tenant for the whole integration run and hands it to the test
 * files via LH_IT_TENANT.
 *
 * Why a tenant at all: LittleHorse metadata is immutable — re-registering a
 * TaskDef with different input vars fails with "already exists and is
 * immutable" — so reusing a namespace across runs makes results depend on
 * whatever earlier runs happened to register. A fresh tenant per run makes
 * the suite hermetic and repeatable.
 *
 * Why one for the whole run rather than one per file: creating tenants from
 * several files in the same run produced "Tenant not allowed" on the
 * second file's requests. One tenant per run avoids that entirely and is
 * cheaper besides.
 */
export default async function globalSetup(): Promise<void> {
  const host = process.env.LH_IT_HOST ?? 'localhost'
  const port = process.env.LH_IT_PORT ?? '2023'
  const client = LHConfig.fromMap({ LHC_API_HOST: host, LHC_API_PORT: port }).getClient()

  // Wait for the server before doing anything else, so a cold container gives
  // a clear "not ready" failure rather than a confusing tenant error.
  const deadline = Date.now() + 90_000
  for (;;) {
    try {
      await client.putTaskDef({ name: 'lh-sdkjs-it-probe', inputVars: [] })
      break
    } catch (err) {
      if (Date.now() > deadline) {
        throw new Error(
          `No LittleHorse server at ${host}:${port} after 90s. Start one with:\n` +
            `  docker run -d --name lh-sdkjs-it -p 2023:2023 \\\n` +
            `    -e LHS_ADVERTISED_LISTENERS=PLAIN://localhost:2023 \\\n` +
            `    ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master\n` +
            `Underlying error: ${(err as Error).message}`
        )
      }
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
  }

  const tenantId = `sdkjs-it-${Date.now().toString(36)}-${Math.floor(Math.random() * 1e6).toString(36)}`
  await client.putTenant({ id: tenantId })
  process.env.LH_IT_TENANT = tenantId
  console.log(`\n[integration] server ${host}:${port}, tenant ${tenantId}`)
}
