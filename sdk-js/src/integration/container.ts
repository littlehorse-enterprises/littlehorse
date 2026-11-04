import { createServer } from 'net'
import { GenericContainer, StartedTestContainer, Wait } from 'testcontainers'

/**
 * Lifecycle for the lh-standalone container backing the integration suite,
 * via Testcontainers (which the Java side of this repo already uses).
 *
 * Every run gets its own container on its own port, so results never depend
 * on a server an earlier run happened to leave behind. That matters more than
 * it sounds: LittleHorse metadata is immutable and its WfSpec registration is
 * eventually consistent, so a warm, previously-used server can hide races and
 * pass tests that a fresh one fails.
 *
 * Testcontainers over raw `docker` calls buys one thing in particular: its
 * Ryuk reaper removes the container even when the run is killed outright
 * (Ctrl-C, CI timeout), where a globalTeardown hook would never fire.
 *
 * Escape hatch: set LH_IT_HOST/LH_IT_PORT to point at a server you manage
 * yourself and no container is started. Measured on this repo: ~19s per run
 * with a fresh container (~14s of it boot) versus ~3.5s reusing one, so the
 * fast inner loop is `LH_IT_KEEP=1` once, then LH_IT_HOST/LH_IT_PORT after.
 */

export const LH_IMAGE = process.env.LH_IT_IMAGE ?? 'ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master'

/** Container port lh-standalone always listens on, regardless of host mapping. */
const CONTAINER_PORT = 2023

export interface StartedContainer {
  container: StartedTestContainer
  name: string
  host: string
  port: number
}

/**
 * Asks the OS for a free port by binding to :0 and releasing it.
 *
 * The host port has to be known *before* the container starts, because
 * LHS_ADVERTISED_LISTENERS must name it: workers ask the server which hosts
 * to poll and connect to whatever it advertises, so advertising the
 * container-internal port hands them an address they cannot reach. That rules
 * out letting Testcontainers pick the mapping for us.
 */
export async function allocatePort(): Promise<number> {
  return new Promise((resolve, reject) => {
    const server = createServer()
    server.unref()
    server.on('error', reject)
    server.listen(0, '127.0.0.1', () => {
      const address = server.address()
      if (address === null || typeof address === 'string') {
        reject(new Error('could not determine a free port'))
        return
      }
      const { port } = address
      server.close(() => resolve(port))
    })
  })
}

/** Starts lh-standalone bound to the given host port. */
export async function startLhStandalone(port: number): Promise<StartedContainer> {
  // Testcontainers would otherwise assign a random Docker name like
  // "interesting_spence"; an explicit one makes the container obvious in
  // `docker ps` while staying unique per run.
  const name = `lh-sdkjs-it-${Date.now().toString(36)}-${Math.floor(Math.random() * 1e6).toString(36)}`

  const container = await new GenericContainer(LH_IMAGE)
    .withName(name)
    .withLabels({ 'io.littlehorse.sdk-js.integration': 'true' })
    .withExposedPorts({ container: CONTAINER_PORT, host: port })
    .withEnvironment({ LHS_ADVERTISED_LISTENERS: `PLAIN://localhost:${port}` })
    // Only says the port is open; globalSetup still polls the API itself,
    // which is the signal that actually matters.
    .withWaitStrategy(Wait.forListeningPorts())
    .withStartupTimeout(180_000)
    .start()

  return {
    container,
    name: container.getName().replace(/^\//, ''),
    host: 'localhost',
    port,
  }
}

export async function stopLhStandalone(container: StartedTestContainer): Promise<void> {
  await container.stop({ remove: true, removeVolumes: true })
}

/** Last ~40 lines of container output, for diagnosing a boot failure. */
export async function containerLogs(container: StartedTestContainer): Promise<string> {
  try {
    const stream = await container.logs({ tail: 40 })
    const chunks: string[] = []
    for await (const chunk of stream) {
      chunks.push(String(chunk))
    }
    return chunks.join('')
  } catch (err) {
    return `(could not read logs: ${(err as Error).message})`
  }
}
