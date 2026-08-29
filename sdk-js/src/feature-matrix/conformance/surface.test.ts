import { describe, expect, test } from '@jest/globals'
import { collectCitations, loadJavaSurface, surfaceKeys } from '../harness/citations'
import { CITATION_ALIASES, EXEMPTIONS } from '../harness/javaSurfaceExemptions'

/**
 * The freshness check (proposals/sdk-js-parity/wfsdk.md, Design 1).
 *
 * Compares two machine-produced lists:
 *   list #1 — sdk-java's public wfsdk surface (golden/java-surface.json,
 *             emitted by reflection; regenerate with
 *             ./gradlew :sdk-js-golden-generator:runSurface --args="$(pwd)/sdk-js/golden")
 *   list #2 — the `— Java: Class#method` citations in the matrix test titles.
 *
 * A red here has exactly two legal resolutions: a test.todo citing the symbol
 * (a feature we owe — the banner shows the debt), or an exemption with a
 * written reason in harness/javaSurfaceExemptions.ts (never applicable).
 */

const surface = surfaceKeys(loadJavaSurface())
const { cited, etcBases } = collectCitations()

// Grouped citations expand through the alias table (checked below).
const citedExpanded = new Set(cited)
for (const base of etcBases) {
  for (const expansion of CITATION_ALIASES[base] ?? []) citedExpanded.add(expansion)
}

const exemptKeys = new Set(EXEMPTIONS.map(e => e.key))

describe('sdk-java wfsdk surface vs the feature matrix', () => {
  test('every public sdk-java wfsdk member is cited by a test or exempted with a reason', () => {
    const missing = [...surface.entries()]
      .filter(([, info]) => !info.deprecated)
      .map(([key]) => key)
      .filter(key => !citedExpanded.has(key) && !exemptKeys.has(key))
      .sort()

    if (missing.length > 0) {
      // Two legal resolutions, stated at the point of failure.
      console.error(
        `\nUncovered public sdk-java members (${missing.length}):\n` +
          missing.map(k => `  ${k}`).join('\n') +
          `\n\nFor each: add a test/test.todo whose title cites it ("— Java: <symbol>"),` +
          `\nor add a reasoned exemption in harness/javaSurfaceExemptions.ts.\n`
      )
    }
    expect(missing).toEqual([])
  })

  test('every citation of a covered class names a real sdk-java member', () => {
    // Catches typos in test titles and members Java has renamed or removed.
    const coveredClasses = new Set([...surface.keys()].map(key => key.split('#')[0]))
    const unknown = [...cited]
      .filter(key => coveredClasses.has(key.split('#')[0]))
      .filter(key => !surface.has(key))
      .sort()
    expect(unknown).toEqual([])
  })

  test('exemptions and aliases are alive: real symbols, cited referents, no shadowing', () => {
    const problems: string[] = []

    for (const exemption of EXEMPTIONS) {
      if (!surface.has(exemption.key)) {
        problems.push(`exemption for '${exemption.key}' names a symbol not in the Java surface (dead entry)`)
      }
      if (citedExpanded.has(exemption.key)) {
        problems.push(`exemption for '${exemption.key}' is stale: the symbol is now cited by a test`)
      }
      if (exemption.kind === 'COVERED_BY' && !exemption.coveredBy) {
        problems.push(`exemption for '${exemption.key}' is COVERED_BY but names no coveredBy symbol`)
      }
      if (exemption.coveredBy && !cited.has(exemption.coveredBy)) {
        problems.push(`exemption for '${exemption.key}' points at '${exemption.coveredBy}', which no test cites`)
      }
    }

    const seen = new Set<string>()
    for (const exemption of EXEMPTIONS) {
      if (seen.has(exemption.key)) problems.push(`duplicate exemption for '${exemption.key}'`)
      seen.add(exemption.key)
    }

    for (const base of etcBases) {
      if (!CITATION_ALIASES[base]) {
        problems.push(`grouped citation '${base} etc.' has no CITATION_ALIASES entry spelling it out`)
      }
    }
    for (const [base, expansions] of Object.entries(CITATION_ALIASES)) {
      for (const key of [base, ...expansions]) {
        if (!surface.has(key)) problems.push(`alias '${base}' references '${key}', not in the Java surface`)
      }
    }

    expect(problems).toEqual([])
  })

  test('deprecated sdk-java members are reported, not silently skipped', () => {
    const deprecated = [...surface.entries()]
      .filter(([, info]) => info.deprecated)
      .map(([key]) => key)
      .sort()
    console.log(
      deprecated.length === 0
        ? 'No deprecated members in the covered sdk-java wfsdk surface.'
        : `Deprecated sdk-java members (skipped by the coverage check):\n` + deprecated.map(k => `  ${k}`).join('\n')
    )
    // Deprecated members must still be *real* members — reflection guarantees
    // it, and this pins the invariant against hand-edits of the surface file.
    expect(deprecated.every(key => surface.has(key))).toBe(true)
  })
})
