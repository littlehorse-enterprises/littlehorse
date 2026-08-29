import * as fs from 'fs'
import * as path from 'path'

/**
 * Side B of the freshness check (proposals/sdk-js-parity/wfsdk.md, Design 1):
 * collect every `— Java: Class#method` citation from the feature-matrix test
 * titles.
 *
 * The sources are read as text rather than executed so that `test.todo`
 * titles count too — a todo is visible coverage-in-progress, and the check's
 * job is to force a todo or an exemption, not a finished test.
 */

const MATRIX_DIR = path.resolve(__dirname, '..')

/** `Class#method`, normalized (dot-statics like `Workflow.newWorkflow` → `#`). */
export interface Citation {
  key: string
  /** The raw citation fragment as written in the test title. */
  raw: string
  file: string
}

function testFilesUnder(dir: string): string[] {
  const out: string[] = []
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name)
    if (entry.isDirectory()) out.push(...testFilesUnder(full))
    else if (entry.name.endsWith('.test.ts')) out.push(full)
  }
  return out
}

/**
 * Extracts citation fragments from one title's `— Java: …` tail. Fragments
 * are comma-separated; each contributes a `Class#method` (or `Class.method`)
 * if it parses, and is skipped otherwise (prose citations like
 * "worker struct mapping" belong to layers this check does not cover yet).
 *
 * A fragment ending in " etc." is a grouped citation; the base symbol counts
 * as cited here, and CITATION_ALIASES (see javaSurfaceExemptions.ts) must
 * expand the rest — the surface test enforces that the alias entry exists.
 */
export function parseCitationTail(tail: string): { keys: string[]; etcBases: string[] } {
  const keys: string[] = []
  const etcBases: string[] = []
  for (const rawFragment of tail.split(',')) {
    const fragment = rawFragment.trim()
    const match = /^([A-Z][A-Za-z]*)[#.]([A-Za-z]+)/.exec(fragment)
    if (!match) continue
    const key = `${match[1]}#${match[2]}`
    keys.push(key)
    if (/\betc\.?$/.test(fragment)) etcBases.push(key)
  }
  return { keys, etcBases }
}

export interface CollectedCitations {
  /** Every normalized `Class#method` cited anywhere in the matrix. */
  cited: Set<string>
  /** Grouped citations (`… etc.`) whose expansion must come from an alias. */
  etcBases: Set<string>
  all: Citation[]
}

export function collectCitations(): CollectedCitations {
  const cited = new Set<string>()
  const etcBases = new Set<string>()
  const all: Citation[] = []

  for (const file of testFilesUnder(MATRIX_DIR)) {
    const source = fs.readFileSync(file, 'utf-8')
    // Titles are single-line quoted strings; capture the tail after "— Java:"
    // up to the closing quote.
    for (const match of source.matchAll(/— Java:\s*([^'"`\n]+)/g)) {
      const tail = match[1]
      const parsed = parseCitationTail(tail)
      for (const key of parsed.keys) {
        cited.add(key)
        all.push({ key, raw: tail.trim(), file: path.relative(MATRIX_DIR, file) })
      }
      for (const base of parsed.etcBases) etcBases.add(base)
    }
  }
  return { cited, etcBases, all }
}

/** The committed output of `./gradlew :sdk-js-golden-generator:runSurface`. */
export interface JavaSurface {
  types: Record<string, Record<string, { overloads: number; deprecated: boolean }>>
}

export function loadJavaSurface(): JavaSurface {
  const file = path.resolve(__dirname, '../../../golden/fixtures/java-surface.json')
  if (!fs.existsSync(file)) {
    throw new Error(
      `${file} is missing. Regenerate it with:\n` +
        `  ./gradlew :sdk-js-golden-generator:runSurface --args="$(pwd)/sdk-js/golden"`
    )
  }
  return JSON.parse(fs.readFileSync(file, 'utf-8')) as JavaSurface
}

/** Flattens the surface to `Class#method` keys. */
export function surfaceKeys(surface: JavaSurface): Map<string, { overloads: number; deprecated: boolean }> {
  const keys = new Map<string, { overloads: number; deprecated: boolean }>()
  for (const [type, methods] of Object.entries(surface.types)) {
    for (const [method, info] of Object.entries(methods)) {
      keys.set(`${type}#${method}`, info)
    }
  }
  return keys
}
