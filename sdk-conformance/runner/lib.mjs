// Shared helpers for the conformance runner. Plain Node, zero dependencies.
import { readFileSync, existsSync } from 'node:fs'
import { execFileSync } from 'node:child_process'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

export const REPO_ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..', '..')
export const CONFORMANCE = resolve(REPO_ROOT, 'sdk-conformance')

export function readJson(path) {
  return JSON.parse(readFileSync(path, 'utf8'))
}

/**
 * Parses the one yaml shape ledgers and exemptions use:
 *   todo:                     todo: []
 *     - id: some-case         not_applicable: []
 *       reason: why
 * Anything else errors — a silently misread excuse is worse than a crash.
 */
export function readLedgerYaml(path) {
  const out = {}
  let currentList = null
  let currentEntry = null
  const lines = readFileSync(path, 'utf8').split('\n')
  for (const [i, raw] of lines.entries()) {
    // yaml comments start the line or follow whitespace — a bare # inside a
    // value (capability ids like Workflow#newWorkflow) is content
    const line = raw.replace(/(^|\s)#.*$/, '$1').trimEnd()
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
  // The TREE hash of areas/, not a commit hash: computed purely from file
  // contents, so identical corpora stamp identically no matter how history
  // was squashed or rebased (a squash-merge once made every stamp "stale").
  const rev = execFileSync('git', ['rev-parse', '--short', 'HEAD:sdk-conformance/areas'], {
    cwd: REPO_ROOT, encoding: 'utf8',
  }).trim()
  const dirty = execFileSync('git', ['status', '--porcelain', '--', 'sdk-conformance/areas'], {
    cwd: REPO_ROOT, encoding: 'utf8',
  }).trim()
  return dirty ? `${rev}+dirty` : rev
}

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
  return { ok: false, stdout: '', stderr: `testee binary not found: ${bin} — build it first (see sdk-conformance/README.md)` }
}

/** Like runTestee, with data piped to the testee's stdin. */
export function runTesteeStdin(command, args, input) {
  const [bin, ...baseArgs] = command
  const binPath = resolve(REPO_ROOT, bin)
  try {
    const stdout = execFileSync(bin.includes('/') ? binPath : bin, [...baseArgs, ...args], {
      cwd: REPO_ROOT, encoding: 'utf8', input, stdio: ['pipe', 'pipe', 'pipe'],
    })
    return { ok: true, stdout }
  } catch (err) {
    return { ok: false, stdout: err.stdout ?? '', stderr: err.stderr ?? String(err) }
  }
}

export function fail(msg) {
  console.error(`\x1b[31m✗ ${msg}\x1b[0m`)
  process.exitCode = 1
}

export function ok(msg) {
  console.log(`\x1b[32m✓\x1b[0m ${msg}`)
}

/**
 * Testees are built by their own SDK's toolchain, so a fresh checkout has
 * none. Returns [{sdk, build}] for every registered testee whose command
 * target is missing, so entry points can print the fix instead of a spawn
 * error.
 */
export function missingTestees() {
  const testees = readJson(resolve(CONFORMANCE, 'testees.json'))
  const missing = []
  for (const [sdk, config] of Object.entries(testees)) {
    const target = config.command[0] === 'node' ? config.command[1] : config.command[0]
    if (!existsSync(resolve(REPO_ROOT, target))) {
      missing.push({ sdk, build: config.build })
    }
  }
  return missing
}
