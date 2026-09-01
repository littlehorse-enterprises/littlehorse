#!/usr/bin/env node
// Random dual-compile (see ../FUZZ.md): every registered testee generates
// the same seeded random workflow; outputs are cross-compared pairwise.
// No canon, no ledger — divergence is reported and exits 1.
import { resolve } from 'node:path'
import { CONFORMANCE, readJson, runTestee, fail, ok } from './lib.mjs'
import { semanticDiff } from './compare.mjs'

const seeds = Number(process.argv[2] ?? 20)
const ops = Number(process.argv[3] ?? 12)
const testees = Object.entries(readJson(resolve(CONFORMANCE, 'testees.json')))

let divergent = 0
for (let seed = 1; seed <= seeds; seed++) {
  const outputs = []
  for (const [sdk, config] of testees) {
    const res = runTestee(config.command, ['fuzz', '--seed', String(seed), '--ops', String(ops)])
    if (!res.ok) { fail(`${sdk}: fuzz --seed ${seed} failed: ${res.stderr.trim().slice(0, 200)}`); continue }
    try { outputs.push([sdk, JSON.parse(res.stdout)]) } catch (e) { fail(`${sdk}: fuzz --seed ${seed}: stdout is not JSON`) }
  }
  for (let i = 1; i < outputs.length; i++) {
    const diffs = semanticDiff(outputs[0][1], outputs[i][1])
    if (diffs.length) {
      divergent++
      fail(`seed ${seed}: ${outputs[0][0]} and ${outputs[i][0]} diverge:`)
      for (const d of diffs.slice(0, 8)) console.error(`    ${d}`)
    }
  }
}
if (divergent === 0) ok(`fuzz: ${seeds} seeds × ${ops} ops — all SDKs agree`)
