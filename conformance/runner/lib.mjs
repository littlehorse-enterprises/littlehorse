// Shared helpers for the conformance runner. Plain Node, zero dependencies.
import { readFileSync, existsSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

export const REPO_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..')
export const CONFORMANCE = resolve(REPO_ROOT, 'conformance')

export function readJson(path) {
  return JSON.parse(readFileSync(path, 'utf8'))
}

/**
 * Parser for the ONE yaml shape the suite uses (ledgers and exemptions):
 *
 *   todo:                     todo: []
 *     - id: some-case         not_applicable: []
 *       reason: why
 *
 * Anything outside that shape is an error — these files are ratcheted
 * records, not general documents, and a silently misread excuse is worse
 * than a crash.
 */
export function readLedgerYaml(path) {
  const out = {}
  let currentList = null
  let currentEntry = null
  const lines = readFileSync(path, 'utf8').split('\n')
  for (const [i, raw] of lines.entries()) {
    const line = raw.replace(/#.*$/, '').trimEnd()
    if (line.trim() === '') continue
    const loc = `${path}:${i + 1}`
    let m
    if ((m = line.match(/^(\w+):\s*\[\]$/))) {
      out[m[1]] = []
      currentList = null
    } else if ((m = line.match(/^(\w+):$/))) {
      out[m[1]] = []
      currentList = out[m[1]]
    } else if ((m = line.match(/^\s+-\s+(\w+):\s*(.+)$/))) {
      if (!currentList) throw new Error(`${loc}: list item outside a list`)
      currentEntry = { [m[1]]: m[2].trim() }
      currentList.push(currentEntry)
    } else if ((m = line.match(/^\s+(\w+):\s*(.+)$/))) {
      if (!currentEntry) throw new Error(`${loc}: field outside a list item`)
      currentEntry[m[1]] = m[2].trim()
    } else {
      throw new Error(`${loc}: unrecognized line: ${raw}`)
    }
  }
  for (const key of Object.keys(out)) {
    if (!['todo', 'not_applicable'].includes(key)) throw new Error(`${path}: unknown section "${key}"`)
  }
  return { todo: out.todo ?? [], not_applicable: out.not_applicable ?? [] }
}

/** Revision of the last commit that touched the corpus, "+dirty" if edited since. */
export function corpusRevision() {
  const rev = execFileSync('git', ['log', '-1', '--format=%h', '--', 'conformance/wfsdk'], {
    cwd: REPO_ROOT, encoding: 'utf8',
  }).trim()
  const dirty = execFileSync('git', ['status', '--porcelain', '--', 'conformance/wfsdk'], {
    cwd: REPO_ROOT, encoding: 'utf8',
  }).trim()
  return dirty ? `${rev}+dirty` : rev
}

/** Run a testee verb; returns {ok, stdout, stderr}. */
export function runTestee(command, args) {
  const [bin, ...baseArgs] = command
  const binPath = resolve(REPO_ROOT, bin)
  if (!bin.includes('/') || existsSync(binPath)) {
    try {
      const stdout = execFileSync(bin.includes('/') ? binPath : bin, [...baseArgs, ...args], {
        cwd: REPO_ROOT, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'],
      })
      return { ok: true, stdout }
    } catch (err) {
      return { ok: false, stdout: err.stdout ?? '', stderr: err.stderr ?? String(err) }
    }
  }
  return { ok: false, stdout: '', stderr: `testee binary not found: ${bin} — build it first (see conformance/README.md)` }
}

export function fail(msg) {
  console.error(`\x1b[31m✗ ${msg}\x1b[0m`)
  process.exitCode = 1
}

export function ok(msg) {
  console.log(`\x1b[32m✓\x1b[0m ${msg}`)
}
