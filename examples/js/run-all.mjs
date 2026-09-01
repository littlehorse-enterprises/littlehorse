#!/usr/bin/env node
// Runs every code-first example against a live LittleHorse server and
// requires a clean exit — the executable proof behind the conformance
// matrix. Skips the lhctl-based walkthroughs. Usage: node run-all.mjs
import { readdirSync, existsSync } from 'node:fs'
import { execSync } from 'node:child_process'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const ROOT = dirname(fileURLToPath(import.meta.url))
// quickstart + simple-worker are lhctl walkthroughs; structs runs
// long-lived workers with a separate run-wf trigger — all manual demos.
const SKIP = new Set(['quickstart', 'simple-worker', 'structs'])
const dirs = readdirSync(ROOT, { withFileTypes: true })
  .filter((d) => d.isDirectory() && !SKIP.has(d.name) && d.name !== 'node_modules')
  .map((d) => d.name)
  .filter((d) => existsSync(join(ROOT, d, 'package.json')))
  .sort()

const failed = []
for (const dir of dirs) {
  const cwd = resolve(ROOT, dir)
  process.stdout.write(`${dir} ... `)
  try {
    execSync('npm install --no-audit --no-fund --silent', { cwd, stdio: 'pipe', timeout: 180000 })
    execSync('npm start --silent', { cwd, stdio: 'pipe', timeout: 180000 })
    console.log('PASS')
  } catch (err) {
    console.log('FAIL')
    failed.push(dir)
    const out = `${err.stdout ?? ''}${err.stderr ?? ''}`.split('\n').filter(Boolean).slice(-6)
    for (const line of out) console.log(`    ${line}`)
  }
}
console.log(`\n${dirs.length - failed.length}/${dirs.length} examples passed${failed.length ? ` — failed: ${failed.join(', ')}` : ''}`)
process.exit(failed.length ? 1 : 0)
