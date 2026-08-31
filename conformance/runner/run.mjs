#!/usr/bin/env node
// The grader. For every registered SDK: ask its testee for its case list,
// compile every declared variant, compare semantically against the frozen
// canon, reconcile with the SDK's ledger (two-way ratchet), and write
// results/<sdk>.json. Exit 1 on any gate failure. See conformance/README.md.
import { writeFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { CONFORMANCE, readJson, readLedgerYaml, corpusRevision, runTestee, fail, ok } from './lib.mjs'
import { semanticDiff } from './compare.mjs'

const manifest = readJson(resolve(CONFORMANCE, 'wfsdk', 'manifest.json'))
const testees = readJson(resolve(CONFORMANCE, 'testees.json'))
const revision = corpusRevision()

for (const [sdk, config] of Object.entries(testees)) {
  const ledger = readLedgerYaml(resolve(CONFORMANCE, 'ledgers', `${sdk}.yaml`))
  const excused = new Map([
    ...ledger.todo.map((e) => [e.id, 'todo']),
    ...ledger.not_applicable.map((e) => [e.id, 'not_applicable']),
  ])
  for (const id of excused.keys())
    if (!manifest.cases.some((c) => c.id === id)) fail(`${sdk}: ledger excuses unknown case "${id}"`)

  const listed = runTestee(config.command, ['list'])
  if (!listed.ok) { fail(`${sdk}: testee 'list' failed: ${listed.stderr.trim()}`); continue }
  const implemented = new Set(listed.stdout.split('\n').map((s) => s.trim()).filter(Boolean))

  const outcomes = {}
  for (const c of manifest.cases) {
    const excuse = excused.get(c.id)
    if (!implemented.has(c.id)) {
      outcomes[c.id] = { outcome: excuse ? 'SKIP' : 'MISSING', ...(excuse && { excuse }) }
      if (!excuse && c.level === 'required') fail(`${sdk}: required case "${c.id}" MISSING from testee list and not in ledgers/${sdk}.yaml`)
      continue
    }
    // Excused-but-listed cases still RUN — that is how the ratchet catches
    // an excuse that has stopped being true.
    let failedVariant = null
    const variants = {}
    for (const variant of c.variants) {
      const expected = readJson(resolve(CONFORMANCE, 'wfsdk', 'cases', c.id, `${variant}.json`))
      const res = runTestee(config.command, ['compile', '--case', c.id, '--variant', variant])
      if (!res.ok) { variants[variant] = 'FAIL'; failedVariant ??= { variant, diffs: [`testee error: ${res.stderr.trim()}`] }; continue }
      let actual
      try { actual = JSON.parse(res.stdout) } catch (e) { variants[variant] = 'FAIL'; failedVariant ??= { variant, diffs: [`stdout is not JSON: ${e.message}`] }; continue }
      const diffs = semanticDiff(expected, actual)
      variants[variant] = diffs.length ? 'FAIL' : 'PASS'
      if (diffs.length) failedVariant ??= { variant, diffs }
    }
    if (!failedVariant) {
      if (excuse) {
        outcomes[c.id] = { outcome: 'PASS', variants, staleExcuse: excuse }
        fail(`${sdk}: case "${c.id}" PASSES but is excused as ${excuse} — delete its line from ledgers/${sdk}.yaml`)
      } else {
        outcomes[c.id] = { outcome: 'PASS', variants }
      }
    } else if (excuse) {
      outcomes[c.id] = { outcome: 'SKIP', excuse, variants }
    } else {
      outcomes[c.id] = { outcome: 'FAIL', variants }
      if (c.level === 'required') {
        fail(`${sdk}: required case "${c.id}" FAILED on variant "${failedVariant.variant}":`)
        for (const d of failedVariant.diffs.slice(0, 20)) console.error(`    ${d}`)
      }
    }
  }

  const summary = { PASS: 0, FAIL: 0, SKIP: 0, MISSING: 0 }
  for (const o of Object.values(outcomes)) summary[o.outcome]++
  writeFileSync(
    resolve(CONFORMANCE, 'results', `${sdk}.json`),
    JSON.stringify({ sdk, corpusRevision: revision, summary, outcomes }, null, 2) + '\n',
  )
  ok(`${sdk}: ${summary.PASS} pass, ${summary.FAIL} fail, ${summary.SKIP} skip, ${summary.MISSING} missing → results/${sdk}.json`)
}
