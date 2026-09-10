import type { StartedTestContainer } from 'testcontainers'
import { stopLhStandalone } from './container'

/**
 * Removes the container started by globalSetup. Nothing to do when the suite
 * ran against an externally managed server.
 *
 * Set LH_IT_KEEP=1 to leave it running — useful when a test failed and you
 * want to inspect server state or logs. Note that if the run is killed
 * outright this hook never fires; Testcontainers' Ryuk reaper cleans up in
 * that case.
 */
export default async function globalTeardown(): Promise<void> {
  const container = (globalThis as Record<string, unknown>).__LH_IT_CONTAINER__ as StartedTestContainer | undefined
  if (container === undefined) return

  const name = process.env.LH_IT_CONTAINER_NAME ?? '(unknown)'

  if (process.env.LH_IT_KEEP === '1') {
    console.log(
      `\n[integration] keeping container ${name} (LH_IT_KEEP=1) on ${process.env.LH_IT_HOST}:${process.env.LH_IT_PORT}\n` +
        `[integration] remove it with: docker rm -f ${name}`
    )
    return
  }

  await stopLhStandalone(container)
  console.log(`\n[integration] removed container ${name}`)
}
