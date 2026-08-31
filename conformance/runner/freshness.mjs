#!/usr/bin/env node
// Corpus self-checks + the surface coverage ratchet. Fails (exit 1) when the
// corpus disagrees with itself or with the reference surface. See
// proposals/sdk-conformance/design.md, "The corpus".
import { readdirSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import { CONFORMANCE, readJson, readLedgerYaml, fail, ok } from './lib.mjs'
import { semanticDiff } from './compare.mjs'

const WFSDK = resolve(CONFORMANCE, 'wfsdk')
const manifest = readJson(resolve(WFSDK, 'manifest.json'))
const surface = readJson(resolve(WFSDK, 'surface.json'))
const exemptions = readLedgerYaml(resolve(WFSDK, 'exemptions.yaml'))

// --- manifest ↔ cases/ directories: exactly 1:1 ---------------------------
const declared = manifest.cases.map((c) => c.id)
const onDisk = readdirSync(resolve(WFSDK, 'cases'), { withFileTypes: true })
  .filter((d) => d.isDirectory())
  .map((d) => d.name)
for (const id of declared) if (!onDisk.includes(id)) fail(`case "${id}" is in manifest.json but has no cases/${id}/ directory`)
for (const id of onDisk) if (!declared.includes(id)) fail(`cases/${id}/ exists but is not in manifest.json`)
if (new Set(declared).size !== declared.length) fail('duplicate case ids in manifest.json')

// --- per-case structure ----------------------------------------------------
for (const c of manifest.cases) {
  const dir = resolve(WFSDK, 'cases', c.id)
  if (!existsSync(resolve(dir, 'scenario.md'))) fail(`case "${c.id}" has no scenario.md`)
  if (!['required', 'recommended'].includes(c.level)) fail(`case "${c.id}" has invalid level "${c.level}"`)
  const fixtures = {}
  for (const variant of c.variants) {
    const f = resolve(dir, `${variant}.json`)
    if (!existsSync(f)) { fail(`case "${c.id}" declares variant "${variant}" but ${variant}.json is missing`); continue }
    try { fixtures[variant] = readJson(f) } catch (e) { fail(`case "${c.id}" ${variant}.json does not parse: ${e.message}`) }
  }
  for (const f of readdirSync(dir)) {
    const m = f.match(/^(.+)\.json$/)
    if (m && !c.variants.includes(m[1])) fail(`case "${c.id}" has undeclared fixture ${f}`)
  }
  // vacuity guard: a pair whose fixtures are identical demonstrates nothing
  if (fixtures.base && fixtures.feature && semanticDiff(fixtures.base, fixtures.feature).length === 0)
    fail(`case "${c.id}" is vacuous: base.json and feature.json are semantically identical`)
}

// --- covers ↔ surface: the coverage ratchet, both directions ---------------
const capabilities = surface.capabilities
const covered = new Set(manifest.cases.flatMap((c) => c.covers))
const excused = new Map([...exemptions.todo, ...exemptions.not_applicable].map((e) => [e.id, e]))
for (const key of covered) if (!capabilities.includes(key)) fail(`case covers "${key}" which is not in surface.json`)
for (const key of capabilities) {
  if (!covered.has(key) && !excused.has(key)) fail(`surface capability "${key}" has no case and no exemptions.yaml entry`)
  if (covered.has(key) && excused.has(key)) fail(`stale excuse: "${key}" is exempted but a case now covers it — delete its exemptions.yaml line`)
}
for (const key of excused.keys()) if (!capabilities.includes(key)) fail(`exemption for "${key}" which is not in surface.json`)

if (process.exitCode !== 1) ok(`corpus fresh: ${manifest.cases.length} case(s) cover ${covered.size}/${capabilities.length} surface capabilities (${excused.size} excused)`)
