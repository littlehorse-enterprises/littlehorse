#!/usr/bin/env node
// Builds every testee registered in testees.json by running each SDK's own
// declared build command. The runner still builds nothing itself; this only
// shells out to the toolchains the registry names.
import { execSync } from 'node:child_process'
import { resolve } from 'node:path'
import { readJson, REPO_ROOT, CONFORMANCE } from './lib.mjs'

const B = '\x1b[1m'
const D = '\x1b[2m'
const X = '\x1b[0m'
const OK = '\x1b[32m✓\x1b[0m'
const NO = '\x1b[31m✗\x1b[0m'

const testees = readJson(resolve(CONFORMANCE, 'testees.json'))

console.log(`\n  ${B}Building testees${X}\n`)

let failed = false
for (const [sdk, { build }] of Object.entries(testees)) {
  if (!build) {
    console.log(`  ${D}- ${sdk}: no build command registered, skipping${X}`)
    continue
  }
  const startedAt = Date.now()
  process.stdout.write(`  ${D}${sdk}:  ${build}${X}\n`)
  try {
    execSync(build, { cwd: REPO_ROOT, stdio: ['ignore', 'pipe', 'pipe'] })
    const seconds = ((Date.now() - startedAt) / 1000).toFixed(1)
    console.log(`  ${OK} ${sdk} built ${D}${seconds}s${X}\n`)
  } catch (err) {
    failed = true
    console.log(`  ${NO} ${sdk} build failed\n`)
    const output = `${err.stdout ?? ''}${err.stderr ?? ''}`
    for (const line of output.split('\n').filter(Boolean).slice(-14)) {
      console.log(`      ${D}${line}${X}`)
    }
    console.log('')
  }
}

process.exit(failed ? 1 : 0)
