import assert from 'node:assert/strict'
import { spawn } from 'node:child_process'
import { once } from 'node:events'
import { createRequire } from 'node:module'
import { createServer } from 'node:net'
import { fileURLToPath } from 'node:url'
import { setTimeout as delay } from 'node:timers/promises'

const require = createRequire(import.meta.url)
const { LHConfig } = require('littlehorse-client')
const cwd = fileURLToPath(new URL('..', import.meta.url))
const config = LHConfig.from({
  apiHost: process.env.LHC_API_HOST || 'localhost',
  apiPort: process.env.LHC_API_PORT || '2023',
})
const client = config.getClient()
let child
let output = ''
let exited = false

async function waitFor(description, check) {
  const deadline = Date.now() + 120_000
  let lastError
  while (Date.now() < deadline) {
    if (exited) throw new Error(`Next.js exited before ${description}\n${output}`)
    try {
      return await check()
    } catch (error) {
      lastError = error
      await delay(500)
    }
  }
  throw new Error(`Timed out waiting for ${description}: ${lastError}\n${output}`)
}

function signalChild(signal) {
  if (!child?.pid || exited) return
  try {
    if (process.platform === 'win32') child.kill(signal)
    else process.kill(-child.pid, signal)
  } catch (error) {
    if (error.code !== 'ESRCH') throw error
  }
}

try {
  await waitFor('LittleHorse backend', () => client.whoami({}, { timeout: 1000 }))
  const server = createServer()
  server.listen(0, '127.0.0.1')
  await once(server, 'listening')
  const { port } = server.address()
  await new Promise(resolve => server.close(resolve))

  child = spawn(
    process.execPath,
    [require.resolve('next/dist/bin/next'), 'dev', '--webpack', '--hostname', '127.0.0.1', '--port', String(port)],
    {
      cwd,
      detached: process.platform !== 'win32',
      env: {
        ...process.env,
        LHD_OAUTH_ENABLED: 'false',
        NEXTAUTH_SECRET: 'dashboard-smoke-test',
        NEXTAUTH_URL: `http://127.0.0.1:${port}`,
      },
      stdio: ['ignore', 'pipe', 'pipe'],
    }
  )
  child.once('exit', () => {
    exited = true
  })
  child.once('error', error => {
    output += error.message
    exited = true
  })
  for (const stream of [child.stdout, child.stderr]) {
    stream.on('data', chunk => {
      output = (output + chunk).slice(-20_000)
    })
  }

  const url = `http://127.0.0.1:${port}/`
  const html = await waitFor('dashboard page', async () => {
    const response = await fetch(url, { signal: AbortSignal.timeout(10_000) })
    assert.equal(response.status, 200, `Dashboard returned HTTP ${response.status}`)
    return response.text()
  })
  const stylesheets = [...html.matchAll(/href="([^"]*\/_next\/static\/css\/[^"]+)"/g)]
  assert.ok(stylesheets.length > 0, 'Dashboard page did not include a stylesheet')
  let css = ''
  for (const [, href] of stylesheets) {
    const response = await fetch(new URL(href.replaceAll('&amp;', '&'), url), { signal: AbortSignal.timeout(10_000) })
    assert.equal(response.status, 200, 'Dashboard stylesheet failed to compile')
    css += await response.text()
  }
  assert.ok(css.includes('.bg-background'), 'Tailwind did not generate configured background styles')
  console.log(`Dashboard page and Tailwind stylesheets compiled successfully on ${process.version}`)
} finally {
  config.close()
  if (child && !exited) {
    signalChild('SIGTERM')
    await Promise.race([once(child, 'exit'), delay(5000)])
    signalChild('SIGKILL')
  }
}
