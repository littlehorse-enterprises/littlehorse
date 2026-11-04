#!/usr/bin/env node
// Regenerates the canon by running the reference testee's mint command, the
// one registered in testees.json. Only ever do this inside a PR, where the
// fixture diff is the review surface.
import { execSync } from 'node:child_process'
import { resolve } from 'node:path'
import { readJson, REPO_ROOT, CONFORMANCE, missingTestees } from './lib.mjs'

const B = '\x1b[1m'
const D = '\x1b[2m'
const X = '\x1b[0m'
const OK = '\x1b[32m✓\x1b[0m'
const NO = '\x1b[31m✗\x1b[0m'

const testees = readJson(resolve(CONFORMANCE, 'testees.json'))
const minters = Object.entries(testees).filter(([, entry]) => entry.mint)

if (minters.length !== 1) {
  console.error(`${NO} expected exactly one testee with a "mint" command in testees.json, found ${minters.length}`)
  process.exit(2)
}
const [sdk, { mint }] = minters[0]

if (missingTestees().some((t) => t.sdk === sdk)) {
  console.error(`${NO} the '${sdk}' testee is not built. Build it first:  node sdk-conformance/runner/build.mjs`)
  process.exit(2)
}

console.log(`\n  ${B}Minting the canon${X} ${D}(reference: ${sdk})${X}\n`)
const startedAt = Date.now()
try {
  const out = execSync(mint, { cwd: REPO_ROOT, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] })
  for (const line of out.split('\n').filter(Boolean)) console.log(`  ${D}${line}${X}`)
  console.log(`  ${OK} canon minted ${D}${((Date.now() - startedAt) / 1000).toFixed(1)}s${X}`)
  console.log(`  ${D}review the fixture diff, then rerun: node sdk-conformance/runner/suite.mjs${X}\n`)
} catch (err) {
  console.log(`  ${NO} mint failed\n`)
  const output = `${err.stdout ?? ''}${err.stderr ?? ''}`
  for (const line of output.split('\n').filter(Boolean).slice(-14)) console.log(`      ${D}${line}${X}`)
  process.exit(1)
}
