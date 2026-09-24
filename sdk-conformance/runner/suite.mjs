#!/usr/bin/env node
// The whole suite as one command: freshness → grade → matrix → fuzz,
// rendered as a single report. Exit 1 if any gate fails.
import { spawn } from 'node:child_process'
import { readdirSync } from 'node:fs'
import { resolve, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { CONFORMANCE, readJson, readLedgerYaml, corpusRevision, missingTestees } from './lib.mjs'

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
const SPINNER_FRAMES = ['⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏']

/**
 * Shows a live "still working" line while a gate runs. On a TTY it animates
 * in place and erases itself so the report renders exactly as if it was
 * never there; elsewhere (CI logs) it prints one plain line instead.
 */
const startSpinner = (label, startedAt) => {
  if (!process.stdout.isTTY) {
    console.log(`  ${D}… ${label}${X}`)
    return () => {}
  }
  let frame = 0
  process.stdout.write('\n')
  const timer = setInterval(() => {
    const icon = SPINNER_FRAMES[frame++ % SPINNER_FRAMES.length]
    process.stdout.write(`\r  ${icon} ${label} ${D}${secs(Date.now() - startedAt)}${X} `)
  }, 100)
  timer.unref?.()
  return () => {
    clearInterval(timer)
    process.stdout.write('\r\x1b[2K\x1b[1A')
  }
}

const gate = (args, label) => {
  const t = Date.now()
  const [script, ...rest] = args
  const stopSpinner = startSpinner(label, t)
  return new Promise((resolveGate) => {
    const child = spawn('node', [resolve(RUNNER, script), ...rest], {
      cwd: resolve(CONFORMANCE, '..'),
    })
    let out = ''
    child.stdout.on('data', (chunk) => (out += chunk))
    child.stderr.on('data', (chunk) => (out += chunk))
    child.on('close', (status) => {
      stopSpinner()
      resolveGate({ ok: status === 0, out, ms: Date.now() - t })
    })
  })
}
const detail = (out) => {
  for (const line of out.split('\n').filter(Boolean).slice(0, 14)) console.log(`      ${D}${line.replace(/\x1b\[[0-9;]*m/g, '')}${X}`)
}

console.log(`\n  ${B}LittleHorse SDK Conformance Suite${X}`)

const notBuilt = missingTestees()
if (notBuilt.length > 0) {
  console.log(`\n  ${NO} testees are not built yet. Build them first:\n`)
  console.log(`      node sdk-conformance/runner/build.mjs`)
  console.log(`\n  or per SDK:\n`)
  for (const { sdk, build } of notBuilt) {
    console.log(`      ${sdk}:  ${build}`)
  }
  console.log(`\n  then rerun this command.\n`)
  process.exit(2)
}

// ─── corpus freshness ──────────────────────────────────────────────────
const fresh = await gate(['freshness.mjs'], 'checking the corpus')
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
const graded = await gate(['run.mjs'], 'grading every SDK on every case')
console.log(`\n  ${B}Grading${X} ${D}${secs(graded.ms)}${X}`)
const gradedSummaries = []
for (const f of readdirSync(resolve(CONFORMANCE, 'results')).filter((f) => f.endsWith('.json')).sort()) {
  const r = readJson(resolve(CONFORMANCE, 'results', f))
  gradedSummaries.push(r.summary)
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
const matrix = await gate(['matrix.mjs'], 'regenerating the matrix')
console.log(`\n  ${B}Matrix${X}`)
console.log(
  matrix.ok
    ? `  ${OK} MATRIX.md regenerated at corpus ${corpusRevision()}`
    : `  ${NO} matrix generation failed`,
)
if (!matrix.ok) { failed = true; detail(matrix.out) }

// ─── random dual-compile ───────────────────────────────────────────────
const SEEDS = 20, OPS = 12
const fuzz = await gate(['fuzz.mjs', String(SEEDS), String(OPS)], `compiling ${SEEDS} random workflows in every SDK`)
console.log(`\n  ${B}Random dual-compile${X} ${D}${secs(fuzz.ms)}${X}`)
console.log(
  fuzz.ok
    ? `  ${OK} ${SEEDS} seeds × ${OPS} ops — every SDK compiled identical workflows`
    : `  ${NO} divergence found`,
)
if (!fuzz.ok) { failed = true; detail(fuzz.out) }

// ─── verdict ───────────────────────────────────────────────────────────
// Tallied from the summaries rendered above, not re-read from disk: a
// concurrent run rewriting results/ must not change this run's verdict.
const passed = gradedSummaries.reduce((n, s) => n + s.PASS, 0)
const failures = gradedSummaries.reduce((n, s) => n + s.FAIL + s.MISSING, 0)
console.log(`\n  ${D}${'─'.repeat(64)}${X}`)
console.log(
  `  ${failed ? `${R}${B}✗ FAIL${X}` : `${G}${B}✓ PASS${X}`}   ` +
    `${passed} graded${failures ? ` · ${R}${failures} failing${X}` : ''} · ` +
    `${SEEDS} random workflows ${fuzz.ok ? 'agree' : `${R}diverge${X}`} · ${gradedSummaries.length} SDKs · ${secs(Date.now() - started)}\n`,
)
process.exit(failed ? 1 : 0)
