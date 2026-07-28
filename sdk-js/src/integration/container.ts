import { execFileSync } from 'child_process'
import { createServer } from 'net'

/**
 * Lifecycle for the lh-standalone container backing the integration suite.
 *
 * Every run gets its own container on its own port, so results never depend
 * on a server an earlier run happened to leave behind. That matters more than
 * it sounds: LittleHorse metadata is immutable and its WfSpec registration is
 * eventually consistent, so a warm, previously-used server can hide races and
 * pass tests that a fresh one fails.
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
  name: string
  host: string
  port: number
}

/** Asks the OS for a free port by binding to :0 and releasing it. */
async function freePort(): Promise<number> {
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

function docker(args: string[]): string {
  return execFileSync('docker', args, { encoding: 'utf-8' }).trim()
}

export function dockerAvailable(): boolean {
  try {
    docker(['version', '--format', '{{.Server.Version}}'])
    return true
  } catch {
    return false
  }
}

/**
 * Starts a uniquely named lh-standalone container on a free host port.
 *
 * The advertised listener must match the *host* port: workers ask the server
 * which hosts to poll, and connect to whatever it advertises. Advertising the
 * container-internal port would hand the worker an address it cannot reach.
 */
export function startLhStandalone(port: number): StartedContainer {
  const name = `lh-sdkjs-it-${Date.now().toString(36)}-${Math.floor(Math.random() * 1e6).toString(36)}`
  docker([
    'run',
    '-d',
    '--name',
    name,
    '-p',
    `${port}:${CONTAINER_PORT}`,
    '-e',
    `LHS_ADVERTISED_LISTENERS=PLAIN://localhost:${port}`,
    LH_IMAGE,
  ])
  return { name, host: 'localhost', port }
}

export async function allocatePort(): Promise<number> {
  return freePort()
}

export function stopLhStandalone(name: string): void {
  try {
    docker(['rm', '-f', name])
  } catch {
    // Already gone; nothing to clean up.
  }
}

/** Last ~40 lines of container output, for diagnosing a boot failure. */
export function containerLogs(name: string): string {
  try {
    return execFileSync('docker', ['logs', '--tail', '40', name], {
      encoding: 'utf-8',
      stdio: ['ignore', 'pipe', 'pipe'],
    })
  } catch (err) {
    return `(could not read logs: ${(err as Error).message})`
  }
}
