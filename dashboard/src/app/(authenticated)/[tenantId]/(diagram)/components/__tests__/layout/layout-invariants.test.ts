/**
 * Deterministic graph-layout quality tests.
 *
 * Fixtures are real PutWfSpecRequest JSONs produced by the sdk-js random
 * workflow generator (lh-random-workflows, seed 20260826) — every one was
 * accepted and executed by a real LittleHorse server. Each thread is pushed
 * through the app's own diagram pipeline (getCycleNodes -> extractNodes /
 * extractEdges -> the exact ELK options of LayoutManager -> reactflow's
 * getSmoothStepPath) and the resulting geometry is checked against the
 * readability invariants in invariants.ts.
 *
 * Modes:
 *  - default: RATCHET — per-fixture defect counts must not exceed
 *    baseline.json. Improvements should be ratcheted in by regenerating the
 *    baseline; regressions fail.
 *  - STRICT=1: every invariant must hold with zero defects. This is the
 *    target state; today it documents exactly how far the layout is from it.
 *  - UPDATE_LAYOUT_BASELINE=1: rewrite baseline.json from the current code.
 */
import { readFileSync, readdirSync, writeFileSync } from 'fs'
import { join } from 'path'
import { PutWfSpecRequest } from 'littlehorse-client/proto'
import { buildScene } from '../../layoutHarness/layoutModel'
import { checkScene, countByType } from '../../layoutHarness/invariants'

const FIXTURE_DIR = join(__dirname, 'fixtures')
const BASELINE_PATH = join(__dirname, 'baseline.json')

const fixtures = readdirSync(FIXTURE_DIR)
  .filter(f => f.endsWith('.json'))
  .sort()

type Baseline = Record<string, { defects: Record<string, number>; crossings: number }>

const loadBaseline = (): Baseline => {
  try {
    return JSON.parse(readFileSync(BASELINE_PATH, 'utf-8'))
  } catch {
    return {}
  }
}

const STRICT = process.env.STRICT === '1'
const UPDATE = process.env.UPDATE_LAYOUT_BASELINE === '1'

describe('diagram layout invariants', () => {
  const measured: Baseline = {}
  const baseline = loadBaseline()

  for (const fixture of fixtures) {
    const request = PutWfSpecRequest.fromJsonString(readFileSync(join(FIXTURE_DIR, fixture), 'utf-8'), {
      ignoreUnknownFields: true,
    })

    for (const [threadName, threadSpec] of Object.entries(request.threadSpecs)) {
      const caseKey = `${fixture}::${threadName}`

      test(caseKey, async () => {
        const scene = await buildScene(threadSpec)
        expect(scene.nodes.length).toBeGreaterThan(0)

        // Layout must be a pure function of the graph: a re-layout (remount,
        // HMR, selection change) may never move anything. Proven, not assumed.
        const rerun = await buildScene(threadSpec)
        const geometry = (s: typeof scene) => ({
          nodes: s.nodes.map(n => ({ id: n.id, rect: n.rect })),
          edges: s.edges.map(e => ({ id: e.id, polyline: e.polyline })),
        })
        expect(geometry(rerun)).toEqual(geometry(scene))

        const { defects, crossings } = checkScene(scene)
        const counts = countByType(defects)
        measured[caseKey] = { defects: counts, crossings }

        if (STRICT) {
          const detail = defects.map(d => `  [${d.type}] ${d.detail}`).join('\n')
          expect(defects.length === 0 ? '' : `\n${detail}\n`).toBe('')
          return
        }
        if (UPDATE) return

        const allowed = baseline[caseKey]
        if (allowed === undefined) {
          // A new fixture/thread must enter through a baseline regeneration,
          // so its starting defect count is a reviewed, committed fact.
          throw new Error(
            `no baseline entry for ${caseKey} — run with UPDATE_LAYOUT_BASELINE=1 and commit baseline.json`
          )
        }
        for (const [type, count] of Object.entries(counts)) {
          const budget = allowed.defects[type] ?? 0
          if (count > budget) {
            const examples = defects
              .filter(d => d.type === type)
              .slice(0, 8)
              .map(d => `  ${d.detail}`)
              .join('\n')
            throw new Error(`${caseKey}: ${type} regressed — ${count} > baseline ${budget}\n${examples}`)
          }
        }
      })
    }
  }

  afterAll(() => {
    if (UPDATE) {
      writeFileSync(BASELINE_PATH, JSON.stringify(measured, null, 2) + '\n')
    }
    // One summary table per run, so the current quality is always visible.
    const totals: Record<string, number> = {}
    let crossings = 0
    for (const entry of Object.values(measured)) {
      crossings += entry.crossings
      for (const [type, count] of Object.entries(entry.defects)) totals[type] = (totals[type] ?? 0) + count
    }
    const lines = Object.entries(totals)
      .sort((a, b) => b[1] - a[1])
      .map(([type, count]) => `  ${type.padEnd(30)} ${count}`)
      .join('\n')
    // eslint-disable-next-line no-console
    console.log(
      `\nlayout invariants — ${Object.keys(measured).length} thread graphs from ${fixtures.length} fixtures\n` +
        `${lines || '  (no defects)'}\n  ${'edge crossings (metric only)'.padEnd(30)} ${crossings}\n`
    )
  })
})
