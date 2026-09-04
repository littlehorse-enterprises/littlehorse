#!/usr/bin/env node
// Runs every code-first example against a live LittleHorse server — the
// executable proof behind the conformance matrix. Each example is a
// long-lived worker (src/worker.ts) plus a trigger (src/run.ts): the harness
// starts the worker, waits for its "ready:" line, fires one run through the
// trigger, requires a clean exit, then stops the worker. Skips the
// lhctl-based walkthroughs. Usage: node run-all.mjs
import { readdirSync, existsSync } from 'node:fs'
import { execSync, spawn } from 'node:child_process'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = dirname(fileURLToPath(import.meta.url))
// quickstart + simple-worker are lhctl walkthroughs. struct-builder is
// skipped here only because it registers a struct named `address` that
// conflicts with struct-def's under NO_SCHEMA_UPDATES on a shared server
// (both mirror Java, which runs the two examples separately); it passes on
// its own.
const SKIP = new Set(['quickstart', 'simple-worker', 'struct-builder'])
const READY_TIMEOUT_MS = 60000
const dirs = readdirSync(ROOT, { withFileTypes: true })
  .filter((d) => d.isDirectory() && !SKIP.has(d.name) && d.name !== 'node_modules')
  .map((d) => d.name)
  .filter((d) => existsSync(join(ROOT, d, 'package.json')))
  .sort()

function startWorker(cwd) {
  const tsx = join(cwd, 'node_modules', '.bin', 'tsx')
  const child = spawn(tsx, ['src/worker.ts'], { cwd, stdio: ['ignore', 'pipe', 'pipe'] })
  let output = ''
  const ready = new Promise((resolvePromise, reject) => {
    const timer = setTimeout(() => reject(new Error(`worker not ready after ${READY_TIMEOUT_MS}ms\n${output}`)), READY_TIMEOUT_MS)
    const onData = (chunk) => {
      output += chunk.toString()
      if (/^ready:/m.test(output)) {
        clearTimeout(timer)
        resolvePromise()
      }
    }
    child.stdout.on('data', onData)
    child.stderr.on('data', onData)
    child.on('exit', (code) => {
      clearTimeout(timer)
      reject(new Error(`worker exited early (code ${code})\n${output}`))
    })
  })
  return { child, ready, getOutput: () => output }
}

async function stopWorker(child) {
  if (child.exitCode !== null) return
  const gone = new Promise((resolvePromise) => child.on('exit', resolvePromise))
  child.kill('SIGTERM')
  const timer = setTimeout(() => child.kill('SIGKILL'), 5000)
  await gone
  clearTimeout(timer)
}

const failed = []
for (const dir of dirs) {
  const cwd = resolve(ROOT, dir)
  process.stdout.write(`${dir} ... `)
  let worker
  try {
    execSync('npm install --no-audit --no-fund --silent', { cwd, stdio: 'pipe', timeout: 180000 })
    worker = startWorker(cwd)
    await worker.ready
    execSync('npm run --silent trigger', { cwd, stdio: 'pipe', timeout: 180000 })
    console.log('PASS')
  } catch (err) {
    console.log('FAIL')
    failed.push(dir)
    const out = `${err.stdout ?? ''}${err.stderr ?? ''}${err.message ?? ''}`.split('\n').filter(Boolean).slice(-6)
    for (const line of out) console.log(`    ${line}`)
  } finally {
    if (worker) await stopWorker(worker.child)
  }
}
console.log(`\n${dirs.length - failed.length}/${dirs.length} examples passed${failed.length ? ` — failed: ${failed.join(', ')}` : ''}`)
process.exit(failed.length ? 1 : 0)
