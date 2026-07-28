import { stopLhStandalone } from './container'

/**
 * Removes the container started by globalSetup. Nothing to do when the suite
 * ran against an externally managed server.
 *
 * Set LH_IT_KEEP=1 to leave it running — useful when a test failed and you
 * want to inspect server state or logs.
 */
export default async function globalTeardown(): Promise<void> {
  const name = process.env.LH_IT_CONTAINER
  if (name === undefined) return

  if (process.env.LH_IT_KEEP === '1') {
    console.log(
      `\n[integration] keeping container ${name} (LH_IT_KEEP=1) on ${process.env.LH_IT_HOST}:${process.env.LH_IT_PORT}\n` +
        `[integration] remove it with: docker rm -f ${name}`
    )
    return
  }

  stopLhStandalone(name)
  console.log(`\n[integration] removed container ${name}`)
}
