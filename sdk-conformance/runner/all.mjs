#!/usr/bin/env node
// The whole suite as one command: freshness → grade → matrix → fuzz,
// rendered as a single report. Exit 1 if any gate fails.
import { spawnSync } from 'node:child_process'
import { readdirSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { CONFORMANCE, readJson, readLedgerYaml, corpusRevision } from './lib.mjs'

const RUNNER = dirname(fileURLToPath(import.meta.url))
const B = '\x1b[1m', D = '\x1b[2m', G = '\x1b[32m', R = '\x1b[31m', X = '\x1b[0m'
const OK = `${G}✓${X}`, NO = `${R}✗${X}`
const started = Date.now()
let failed = false

const secs = (ms) => `${(ms / 1000).toFixed(1)}s`
const bar = (n, total, width = 22) => {
  const fill = total === 0 ? width : Math.round((n / total) * width)
  return `${G}${'█'.repeat(fill)}${X}${D}${'░'.repeat(width - fill)}${X}`
}
const gate = (args) => {
  const t = Date.now()
  const [script, ...rest] = args
  const res = spawnSync('node', [resolve(RUNNER, script), ...rest], {
    cwd: resolve(CONFORMANCE, '..'), encoding: 'utf8',
  })
  return { ok: res.status === 0, out: `${res.stdout}${res.stderr}`, ms: Date.now() - t }
}
const detail = (out) => {
  for (const line of out.split('\n').filter(Boolean).slice(0, 14)) console.log(`      ${D}${line.replace(/\x1b\[[0-9;]*m/g, '')}${X}`)
}

console.log(`\n  ${B}LittleHorse SDK Conformance Suite${X}`)

// ─── corpus freshness ──────────────────────────────────────────────────
const fresh = gate(['freshness.mjs'])
console.log(`\n  ${B}Corpus freshness${X} ${D}${secs(fresh.ms)}${X}`)
const AREA_META = { wfsdk: 'capabilities', registrations: 'capabilities', serde: 'arms' }
for (const area of readdirSync(resolve(CONFORMANCE, 'areas')).sort()) {
  const dir = resolve(CONFORMANCE, 'areas', area)
  const cases = readJson(resolve(dir, 'manifest.json')).cases
  const surface = readJson(resolve(dir, 'surface.json'))
  const denom = surface.capabilities ?? surface.arms
  const ex = readLedgerYaml(resolve(dir, 'exemptions.yaml'))
  const excused = ex.todo.length + ex.not_applicable.length
  const covered = denom.length - excused
  console.log(
    `  ${fresh.ok ? OK : NO} ${area.padEnd(14)} ${bar(covered, denom.length)}  ` +
      `${String(covered).padStart(3)}/${denom.length} ${AREA_META[area]}` +
      `${D} · ${cases.length} cases · ${excused} excused${X}`,
  )
}
if (!fresh.ok) { failed = true; detail(fresh.out) }

// ─── grading ───────────────────────────────────────────────────────────
const graded = gate(['run.mjs'])
console.log(`\n  ${B}Grading${X} ${D}${secs(graded.ms)}${X}`)
for (const f of readdirSync(resolve(CONFORMANCE, 'results')).filter((f) => f.endsWith('.json')).sort()) {
  const r = readJson(resolve(CONFORMANCE, 'results', f))
  const total = Object.keys(r.outcomes).length
  const byArea = {}
  for (const o of Object.values(r.outcomes)) byArea[o.area] = (byArea[o.area] ?? 0) + 1
  const good = r.summary.FAIL === 0 && r.summary.MISSING === 0
  const tail =
    `${String(r.summary.PASS).padStart(3)}/${total} passed` +
    (r.summary.FAIL ? ` ${R}${r.summary.FAIL} failed${X}` : '') +
    (r.summary.SKIP ? ` ${r.summary.SKIP} todo` : '') +
    (r.summary.MISSING ? ` ${R}${r.summary.MISSING} missing${X}` : '')
  console.log(
    `  ${good ? OK : NO} ${r.sdk.padEnd(14)} ${bar(r.summary.PASS, total)}  ${tail}` +
      `${D} · ${Object.entries(byArea).map(([a, n]) => `${a} ${n}`).join(' · ')}${X}`,
  )
}
if (!graded.ok) { failed = true; detail(graded.out) }

// ─── matrix ────────────────────────────────────────────────────────────
const matrix = gate(['matrix.mjs'])
console.log(`\n  ${B}Matrix${X}`)
console.log(
  matrix.ok
    ? `  ${OK} MATRIX.md regenerated at corpus ${corpusRevision()}`
    : `  ${NO} matrix generation failed`,
)
if (!matrix.ok) { failed = true; detail(matrix.out) }

// ─── random dual-compile ───────────────────────────────────────────────
const SEEDS = 20, OPS = 12
const fuzz = gate(['fuzz.mjs', String(SEEDS), String(OPS)])
console.log(`\n  ${B}Random dual-compile${X} ${D}${secs(fuzz.ms)}${X}`)
console.log(
  fuzz.ok
    ? `  ${OK} ${SEEDS} seeds × ${OPS} ops — every SDK compiled identical workflows`
    : `  ${NO} divergence found`,
)
if (!fuzz.ok) { failed = true; detail(fuzz.out) }

// ─── verdict ───────────────────────────────────────────────────────────
const results = readdirSync(resolve(CONFORMANCE, 'results')).filter((f) => f.endsWith('.json'))
const totals = results.map((f) => readJson(resolve(CONFORMANCE, 'results', f)).summary)
const passed = totals.reduce((n, s) => n + s.PASS, 0)
const failures = totals.reduce((n, s) => n + s.FAIL + s.MISSING, 0)
console.log(`\n  ${D}${'─'.repeat(64)}${X}`)
console.log(
  `  ${failed ? `${R}${B}✗ FAIL${X}` : `${G}${B}✓ PASS${X}`}   ` +
    `${passed} graded${failures ? ` · ${R}${failures} failing${X}` : ''} · ` +
    `${SEEDS} random workflows agree · ${results.length} SDKs · ${secs(Date.now() - started)}\n`,
)
process.exit(failed ? 1 : 0)
