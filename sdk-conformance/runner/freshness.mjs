#!/usr/bin/env node
// Exit 1 when a corpus area disagrees with itself or its denominator
// (each area's rules.md defines both).
import { readdirSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import { CONFORMANCE, readJson, readLedgerYaml, fail, ok } from './lib.mjs'
import { semanticDiff } from './compare.mjs'

// ═══════════════════════════════════════════════ area conventions
// A directory under areas/ IS an area; one the grader cannot grade would
// render as covered while examining nothing.
const KNOWN_AREAS = ['wfsdk', 'serde', 'registrations']
const discoveredAreas = readdirSync(resolve(CONFORMANCE, 'areas'), { withFileTypes: true })
  .filter((d) => d.isDirectory())
  .map((d) => d.name)
for (const area of discoveredAreas) {
  if (!KNOWN_AREAS.includes(area)) fail(`area "${area}" exists but the runner has no grading strategy for it`)
  if (!existsSync(resolve(CONFORMANCE, 'areas', area, 'rules.md'))) fail(`area "${area}" has no rules.md`)
  if (!existsSync(resolve(CONFORMANCE, 'areas', area, 'manifest.json'))) fail(`area "${area}" has no manifest.json`)
  if (!existsSync(resolve(CONFORMANCE, 'areas', area, 'surface.json'))) fail(`area "${area}" has no surface.json (the denominator — see its rules.md)`)
}
for (const area of KNOWN_AREAS) {
  if (!discoveredAreas.includes(area)) fail(`runner grades area "${area}" but areas/${area}/ does not exist`)
}

// The deeper checks read the files these conventions guarantee — bail
// instead of stack-tracing on their absence.
if (process.exitCode === 1) {
  console.error('area conventions failed — deeper corpus checks skipped')
  process.exit(1)
}

const summaries = []
const idsByArea = {}

/**
 * Pair-style area (wfsdk, registrations): cases/<id>/ directories holding
 * scenario.md + variant fixtures; covers reconciled against surface.json
 * capabilities and exemptions.yaml, ratcheted both ways.
 */
function checkPairArea(area) {
  const DIR = resolve(CONFORMANCE, 'areas', area)
  const manifest = readJson(resolve(DIR, 'manifest.json'))
  const capabilities = readJson(resolve(DIR, 'surface.json')).capabilities
  const exemptions = readLedgerYaml(resolve(DIR, 'exemptions.yaml'))

  const declared = manifest.cases.map((c) => c.id)
  idsByArea[area] = declared
  const onDisk = readdirSync(resolve(DIR, 'cases'), { withFileTypes: true })
    .filter((d) => d.isDirectory())
    .map((d) => d.name)
  for (const id of declared) if (!onDisk.includes(id)) fail(`${area} case "${id}" is in manifest.json but has no cases/${id}/ directory`)
  for (const id of onDisk) if (!declared.includes(id)) fail(`${area} cases/${id}/ exists but is not in manifest.json`)
  if (new Set(declared).size !== declared.length) fail(`duplicate case ids in ${area} manifest.json`)

  for (const c of manifest.cases) {
    const dir = resolve(DIR, 'cases', c.id)
    if (!existsSync(resolve(dir, 'scenario.md'))) fail(`${area} case "${c.id}" has no scenario.md`)
    if (!['required', 'recommended'].includes(c.level)) fail(`${area} case "${c.id}" has invalid level "${c.level}"`)
    const fixtures = {}
    for (const variant of c.variants) {
      const f = resolve(dir, `${variant}.json`)
      if (!existsSync(f)) { fail(`${area} case "${c.id}" declares variant "${variant}" but ${variant}.json is missing`); continue }
      try { fixtures[variant] = readJson(f) } catch (e) { fail(`${area} case "${c.id}" ${variant}.json does not parse: ${e.message}`) }
    }
    for (const f of readdirSync(dir)) {
      const m = f.match(/^(.+)\.json$/)
      if (m && !c.variants.includes(m[1])) fail(`${area} case "${c.id}" has undeclared fixture ${f}`)
    }
    // vacuity guard: a pair whose fixtures are identical demonstrates nothing
    if (fixtures.base && fixtures.feature && semanticDiff(fixtures.base, fixtures.feature).length === 0)
      fail(`${area} case "${c.id}" is vacuous: base.json and feature.json are semantically identical`)
  }

  const covered = new Set(manifest.cases.flatMap((c) => c.covers))
  const excused = new Map([...exemptions.todo, ...exemptions.not_applicable].map((e) => [e.id, e]))
  for (const key of covered) if (!capabilities.includes(key)) fail(`${area} case covers "${key}" which is not in surface.json`)
  for (const key of capabilities) {
    if (!covered.has(key) && !excused.has(key)) fail(`${area} capability "${key}" has no case and no exemptions.yaml entry`)
    if (covered.has(key) && excused.has(key)) fail(`stale excuse: "${key}" is exempted but a ${area} case now covers it — delete its exemptions.yaml line`)
  }
  for (const key of excused.keys()) if (!capabilities.includes(key)) fail(`${area} exemption for "${key}" which is not in surface.json`)

  summaries.push(`${area} ${manifest.cases.length} case(s) cover ${covered.size}/${capabilities.length} capabilities (${excused.size} excused)`)
}

checkPairArea('wfsdk')
checkPairArea('registrations')

// ═══════════════════════════════════════════════════════════ serde area
const SERDE = resolve(CONFORMANCE, 'areas', 'serde')
const serdeManifest = readJson(resolve(SERDE, 'manifest.json'))
const arms = readJson(resolve(SERDE, 'surface.json')).arms
const serdeExemptions = readLedgerYaml(resolve(SERDE, 'exemptions.yaml'))
const INPUT_TYPES = ['str', 'int', 'double', 'bool', 'bytes', 'timestamp', 'json-obj', 'json-arr', 'null']

const serdeIds = serdeManifest.cases.map((c) => c.id)
idsByArea.serde = serdeIds
const serdeOnDisk = readdirSync(resolve(SERDE, 'cases')).filter((f) => f.endsWith('.json')).map((f) => f.replace(/\.json$/, ''))
for (const id of serdeIds) if (!serdeOnDisk.includes(id)) fail(`serde case "${id}" is in manifest.json but cases/${id}.json is missing`)
for (const id of serdeOnDisk) if (!serdeIds.includes(id)) fail(`serde cases/${id}.json exists but is not in manifest.json`)
if (new Set(serdeIds).size !== serdeIds.length) fail('duplicate case ids in serde manifest.json')

// input well-formedness: rules.md S1, S4, S5
const coveredArms = new Set()
for (const c of serdeManifest.cases) {
  if (!['required', 'recommended'].includes(c.level)) fail(`serde case "${c.id}" has invalid level "${c.level}"`)
  if (!INPUT_TYPES.includes(c.input.type)) fail(`serde case "${c.id}" has unknown input type "${c.input.type}"`)
  if (c.input.type === 'null' && c.input.value !== undefined) fail(`serde case "${c.id}": null inputs carry no value`)
  if (c.input.type !== 'null' && typeof c.input.value !== 'string')
    fail(`serde case "${c.id}": input value must be a string (S1/S4 — JSON numbers are lossy)`)
  if (c.input.type === 'json-obj' || c.input.type === 'json-arr') {
    try {
      const parsed = JSON.parse(c.input.value)
      const leaves = c.input.type === 'json-obj' ? Object.values(parsed) : parsed
      if (leaves.length > 1) fail(`serde case "${c.id}": embedded JSON carries at most one key/element (S5)`)
      if (!leaves.every((v) => typeof v === 'string')) fail(`serde case "${c.id}": embedded JSON leaves must be strings (S5)`)
      if (JSON.stringify(parsed) !== c.input.value) fail(`serde case "${c.id}": embedded JSON input must be in compact form (S5)`)
    } catch (e) { if (!String(e).includes('serde case')) fail(`serde case "${c.id}": embedded JSON does not parse: ${e.message}`) }
  }
  try {
    const fixture = readJson(resolve(SERDE, 'cases', `${c.id}.json`))
    for (const arm of Object.keys(fixture)) coveredArms.add(arm)
  } catch (e) { fail(`serde case "${c.id}" fixture does not parse: ${e.message}`) }
}

// arm coverage ratchet: rules.md S3
const excusedArms = new Map([...serdeExemptions.todo, ...serdeExemptions.not_applicable].map((e) => [e.id, e]))
for (const arm of coveredArms) if (!arms.includes(arm)) fail(`serde fixture uses arm "${arm}" which is not in surface.json`)
for (const arm of arms) {
  if (!coveredArms.has(arm) && !excusedArms.has(arm)) fail(`VariableValue arm "${arm}" has no case and no serde exemptions.yaml entry`)
  if (coveredArms.has(arm) && excusedArms.has(arm)) fail(`stale excuse: arm "${arm}" is exempted but a case now covers it — delete its serde exemptions.yaml line`)
}
for (const arm of excusedArms.keys()) if (!arms.includes(arm)) fail(`serde exemption for "${arm}" which is not in surface.json`)
summaries.push(`serde ${serdeManifest.cases.length} case(s) cover ${coveredArms.size}/${arms.length} arms (${excusedArms.size} excused)`)

const seen = new Map()
for (const [area, ids] of Object.entries(idsByArea)) {
  for (const id of ids) {
    if (seen.has(id)) fail(`case id "${id}" exists in both ${seen.get(id)} and ${area} — ids must be unique across the corpus`)
    seen.set(id, area)
  }
}

if (process.exitCode !== 1) ok(`corpus fresh: ${summaries.join('; ')}`)
