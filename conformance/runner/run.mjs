#!/usr/bin/env node
// Grades every registered SDK against the canon; writes results/<sdk>.json;
// exit 1 on any gate failure. Contract: conformance/README.md.
import { writeFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { CONFORMANCE, readJson, readLedgerYaml, corpusRevision, runTestee, runTesteeStdin, fail, ok } from './lib.mjs'
import { semanticDiff } from './compare.mjs'

const wfsdkManifest = readJson(resolve(CONFORMANCE, 'areas', 'wfsdk', 'manifest.json'))
const serdeManifest = readJson(resolve(CONFORMANCE, 'areas', 'serde', 'manifest.json'))
const testees = readJson(resolve(CONFORMANCE, 'testees.json'))
const revision = corpusRevision()
const allCaseIds = new Set([...wfsdkManifest.cases, ...serdeManifest.cases].map((c) => c.id))

function attempt(config, units) {
  let firstFailure = null
  const outcomes = {}
  for (const { name, args, expected, pre } of units) {
    let actual = pre
    if (actual === undefined) {
      const res = runTestee(config.command, args)
      if (!res.ok) { outcomes[name] = 'FAIL'; firstFailure ??= { unit: name, diffs: [`testee error: ${res.stderr.trim()}`] }; continue }
      try { actual = JSON.parse(res.stdout) } catch (e) { outcomes[name] = 'FAIL'; firstFailure ??= { unit: name, diffs: [`stdout is not JSON: ${e.message}`] }; continue }
    }
    const diffs = semanticDiff(expected, actual)
    outcomes[name] = diffs.length ? 'FAIL' : 'PASS'
    if (diffs.length) firstFailure ??= { unit: name, diffs }
  }
  return { outcomes, firstFailure }
}

// Optional accelerator verbs: one process answers everything. A testee
// without them falls back to one spawn per unit.
function batchAnswers(config, serdeManifest) {
  const all = runTestee(config.command, ['compile-all'])
  let wfsdk = null
  if (all.ok) { try { wfsdk = JSON.parse(all.stdout) } catch { wfsdk = null } }
  const input = serdeManifest.cases
    .map((c) => JSON.stringify({ id: c.id, type: c.input.type, value: c.input.value }))
    .join('\n')
  const conv = runTesteeStdin(config.command, ['convert-batch'], input)
  let serde = null
  if (conv.ok) { try { serde = JSON.parse(conv.stdout) } catch { serde = null } }
  return { wfsdk, serde }
}

for (const [sdk, config] of Object.entries(testees)) {
  const ledger = readLedgerYaml(resolve(CONFORMANCE, 'ledgers', `${sdk}.yaml`))
  const excused = new Map([
    ...ledger.todo.map((e) => [e.id, 'todo']),
    ...ledger.not_applicable.map((e) => [e.id, 'not_applicable']),
  ])
  for (const id of excused.keys())
    if (!allCaseIds.has(id)) fail(`${sdk}: ledger excuses unknown case "${id}"`)

  const batch = batchAnswers(config, serdeManifest)
  const listed = runTestee(config.command, ['list'])
  if (!listed.ok) { fail(`${sdk}: testee 'list' failed: ${listed.stderr.trim()}`); continue }
  const implemented = new Set(listed.stdout.split('\n').map((s) => s.trim()).filter(Boolean))

  const outcomes = {}
  const grade = (area, c, units) => {
    const excuse = excused.get(c.id)
    if (!implemented.has(c.id)) {
      outcomes[c.id] = { area, outcome: excuse ? 'SKIP' : 'MISSING', ...(excuse && { excuse }) }
      if (!excuse && c.level === 'required') fail(`${sdk}: required case "${c.id}" MISSING from testee list and not in ledgers/${sdk}.yaml`)
      return
    }
    // Excused-but-listed cases still RUN — that is how the ratchet catches
    // an excuse that has stopped being true.
    const { outcomes: units_, firstFailure } = attempt(config, units)
    if (!firstFailure) {
      outcomes[c.id] = { area, outcome: 'PASS', units: units_, ...(excuse && { staleExcuse: excuse }) }
      if (excuse) fail(`${sdk}: case "${c.id}" PASSES but is excused as ${excuse} — delete its line from ledgers/${sdk}.yaml`)
    } else if (excuse) {
      outcomes[c.id] = { area, outcome: 'SKIP', excuse, units: units_ }
    } else {
      outcomes[c.id] = { area, outcome: 'FAIL', units: units_ }
      if (c.level === 'required') {
        fail(`${sdk}: required case "${c.id}" FAILED on "${firstFailure.unit}":`)
        for (const d of firstFailure.diffs.slice(0, 20)) console.error(`    ${d}`)
      }
    }
  }

  for (const c of wfsdkManifest.cases) {
    grade('wfsdk', c, c.variants.map((variant) => ({
      name: variant,
      args: ['compile', '--case', c.id, '--variant', variant],
      expected: readJson(resolve(CONFORMANCE, 'areas', 'wfsdk', 'cases', c.id, `${variant}.json`)),
      pre: batch.wfsdk?.[`${c.id}/${variant}`],
    })))
  }
  for (const c of serdeManifest.cases) {
    const args = ['convert', '--type', c.input.type]
    if (c.input.value !== undefined) args.push('--value', String(c.input.value))
    grade('serde', c, [{ name: 'convert', args, expected: readJson(resolve(CONFORMANCE, 'areas', 'serde', 'cases', `${c.id}.json`)), pre: batch.serde?.[c.id] }])
  }

  const summary = { PASS: 0, FAIL: 0, SKIP: 0, MISSING: 0 }
  for (const o of Object.values(outcomes)) summary[o.outcome]++
  writeFileSync(
    resolve(CONFORMANCE, 'results', `${sdk}.json`),
    JSON.stringify({ sdk, corpusRevision: revision, summary, outcomes }, null, 2) + '\n',
  )
  ok(`${sdk}: ${summary.PASS} pass, ${summary.FAIL} fail, ${summary.SKIP} skip, ${summary.MISSING} missing → results/${sdk}.json`)
}
