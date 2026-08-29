import { describe, expect, test } from '@jest/globals'
import { listProbeGoldens } from '../harness/golden'
import { loadJavaSurface, surfaceKeys } from '../harness/citations'
import { PROBE_COVERS, probePairs, probeSingles, provenByProbe, provenBySingleProbe } from '../harness/probes'
import { PROBE_BACKLOG } from '../harness/probeBacklog'
import { PROBE_EXEMPTIONS } from '../harness/probeExemptions'

/**
 * The probe coverage guarantee (proposals/sdk-js-parity/wfsdk.md, Design 2).
 *
 * Two duties. First, the registry is self-running: every probe pair's three
 * assertions execute here regardless of which matrix entries cite it, so a
 * probe cannot exist without being enforced, and a registry entry with
 * missing fixtures fails on its own. Second, the ratchet: every java-surface
 * member is either covered by a probe, exempted with a written reason, or
 * listed in the backlog — and the unprobed remainder must equal the backlog
 * EXACTLY, so landing a probe forces deleting its backlog line in the same
 * change, and the backlog can never silently grow.
 */

const pairNames = Object.keys(probePairs).sort()
const singleNames = Object.keys(probeSingles).sort()

describe('probe registry', () => {
  test.each(pairNames)('probe pair "%s": JS ≡ Java (base and feature), and the pair differs', name => {
    provenByProbe(name)
  })

  test.each(singleNames)('single probe "%s": JS ≡ Java', name => {
    provenBySingleProbe(name)
  })

  test('every probe declares what it covers, and covers only real probes', () => {
    const registry = [...pairNames, ...singleNames].sort()
    expect(Object.keys(PROBE_COVERS).sort()).toEqual(registry)
    for (const covers of Object.values(PROBE_COVERS)) {
      expect(covers.length).toBeGreaterThan(0)
    }
  })

  test('the fixtures on disk are exactly the registry — no orphans, no leftovers', () => {
    // A fixture with no registry entry is a stale artifact (a Java-side probe
    // whose TS twin was never written, or a renamed probe's leftovers) that
    // would otherwise rot silently.
    const expected = [...pairNames.flatMap(name => [`${name}.base`, `${name}.feature`]), ...singleNames].sort()
    expect(listProbeGoldens()).toEqual(expected)
  })
})

describe('probe coverage ratchet', () => {
  const surface = [...surfaceKeys(loadJavaSurface()).keys()]
  const surfaceSet = new Set(surface)
  const covered = new Set(Object.values(PROBE_COVERS).flat())
  const exempt = new Set(PROBE_EXEMPTIONS.map(e => e.key))
  const backlog = new Set(PROBE_BACKLOG)

  test('every covers/exemption/backlog entry names a real surface member', () => {
    const ghosts = [...covered, ...exempt, ...backlog].filter(key => !surfaceSet.has(key))
    expect(ghosts).toEqual([])
  })

  test('covered, exempted, and backlogged are disjoint — no double-booked excuses', () => {
    const overlaps = [
      ...[...covered].filter(k => exempt.has(k)).map(k => `covered AND exempt: ${k}`),
      ...[...covered].filter(k => backlog.has(k)).map(k => `covered AND backlogged: ${k}`),
      ...[...exempt].filter(k => backlog.has(k)).map(k => `exempt AND backlogged: ${k}`),
    ]
    expect(overlaps).toEqual([])
  })

  test('the unprobed remainder equals the backlog exactly', () => {
    const missing = surface.filter(key => !covered.has(key) && !exempt.has(key)).sort()
    // A key on the left only: a new/unprobed member — add a probe or triage it.
    // A key on the right only: its probe landed — delete its backlog line.
    expect(missing).toEqual([...PROBE_BACKLOG].sort())
  })
})
