/**
 * Summarizes a run by area instead of just totals.
 *
 * The feature matrix (src/feature-matrix/areas/) is this project's status
 * artifact: passed = a Java SDK capability we've ported and proven, todo = one
 * we haven't. A bare "224 passed" hides that, so this prints per-area parity and
 * separates matrix coverage from the supporting tests that back it up.
 *
 * See proposals/sdk-js-parity/.
 */

const AREA_LABELS = {
  'workflow-constructs': 'workflow constructs execute',
  'worker-runtime': 'worker behavior the server drives',
  wfsdk: 'wfsdk (workflow DSL)',
  worker: 'worker (task runtime)',
  config: 'config + client',
  common: 'common (serde)',
  usertask: 'usertask',
  'wfspec-acceptance': 'WfSpec accepted by server',
  execution: 'end-to-end execution',
  cluster: 'rebalancing across a cluster',
  tls: 'TLS to a real listener',
  oauth: 'OAuth against a real issuer',
}

const SUPPORT_LABELS = {
  golden: 'golden harness (goldens parse + round-trip)',
  'wfsdk-golden': 'golden conformance (TS output == Java output)',
  probes: 'probe pairs (base+feature ≡ Java, and they differ)',
  surface: 'freshness (Java surface vs matrix citations)',
}

const useColor = process.stdout.isTTY && process.env.NO_COLOR === undefined
const paint = (code, text) => (useColor ? `\u001b[${code}m${text}\u001b[0m` : text)
const bold = text => paint('1', text)
const green = text => paint('32', text)
const yellow = text => paint('33', text)
const red = text => paint('31', text)
const dim = text => paint('2', text)

function basename(filePath) {
  const file = filePath.split('/').pop() || ''
  return file.replace(/\.test\.ts$/, '')
}

/**
 * Under feature-matrix/, the *directory* decides what a suite counts as:
 * areas/ holds the enumerated Java capabilities and is the denominator the
 * parity percentage is computed over; conformance/ and harness/ support it.
 * Keying off the directory rather than the filename means adding a suite
 * cannot silently change what the banner claims.
 */
function classify(filePath) {
  if (filePath.includes('/feature-matrix/areas/')) return { kind: 'matrix', key: basename(filePath) }
  if (filePath.includes('/feature-matrix/')) return { kind: 'support', key: basename(filePath) }
  if (filePath.includes('/integration/')) return { kind: 'integration', key: basename(filePath) }
  return { kind: 'support', key: 'unit (config, retry, utils)' }
}

function bar(done, total, width = 20) {
  if (total === 0) return ''
  const filled = Math.round((done / total) * width)
  return green('█'.repeat(filled)) + dim('░'.repeat(width - filled))
}

function pad(text, width) {
  return text.length >= width ? text : text + ' '.repeat(width - text.length)
}

function padStart(text, width) {
  const str = String(text)
  return str.length >= width ? str : ' '.repeat(width - str.length) + str
}

class MatrixReporter {
  onRunComplete(_contexts, results) {
    const groups = new Map()

    for (const suite of results.testResults) {
      const { kind, key } = classify(suite.testFilePath)
      if (!groups.has(key)) groups.set(key, { kind, passed: 0, todo: 0, failed: 0 })
      const group = groups.get(key)
      for (const test of suite.testResults) {
        if (test.status === 'passed') group.passed++
        else if (test.status === 'todo' || test.status === 'pending') group.todo++
        else if (test.status === 'failed') group.failed++
      }
    }

    const matrix = [...groups.entries()].filter(([, g]) => g.kind === 'matrix')
    const integration = [...groups.entries()].filter(([, g]) => g.kind === 'integration')
    const support = [...groups.entries()].filter(([, g]) => g.kind === 'support')
    if (matrix.length === 0 && integration.length === 0 && support.length === 0) return

    const lines = ['']
    const labelOf = ([key, g]) => (g.kind === 'support' ? SUPPORT_LABELS[key] : AREA_LABELS[key]) || key
    // Size the label column to the longest label so both sections line up.
    const labelWidth =
      Math.max(...[...groups.entries()].map(entry => labelOf(entry).length), 'enumerated surface'.length) + 2

    if (matrix.length > 0) {
      lines.push(bold('  Feature matrix — Java SDK capabilities proven in JS'))
      lines.push('')
      matrix.sort((a, b) => b[1].passed + b[1].todo - (a[1].passed + a[1].todo))

      let totalDone = 0
      let totalTodo = 0
      let totalFailed = 0

      for (const [key, g] of matrix) {
        const total = g.passed + g.todo + g.failed
        totalDone += g.passed
        totalTodo += g.todo
        totalFailed += g.failed
        const pct = total === 0 ? 0 : Math.round((g.passed / total) * 100)
        const failedPart = g.failed > 0 ? red(`  ${g.failed} failed`) : ''
        const todoPart = g.todo > 0 ? yellow(`${padStart(g.todo, 3)} todo`) : dim('        ')
        lines.push(
          `  ${pad(AREA_LABELS[key] || key, labelWidth)}${green(padStart(g.passed, 4) + ' done')}  ${todoPart}  ` +
            `${bar(g.passed, total)} ${padStart(pct + '%', 4)}${failedPart}`
        )
      }

      const grandTotal = totalDone + totalTodo + totalFailed
      const pct = grandTotal === 0 ? 0 : Math.round((totalDone / grandTotal) * 100)
      lines.push('')
      lines.push(
        `  ${pad('enumerated surface', labelWidth)}${green(padStart(totalDone, 4) + ' done')}  ` +
          `${totalTodo > 0 ? yellow(padStart(totalTodo, 3) + ' todo') : dim('        ')}  ${bold(pct + '% covered')}` +
          (totalFailed > 0 ? red(`  ${totalFailed} FAILED`) : '')
      )
    }

    if (integration.length > 0) {
      lines.push(bold('  Verified against a real LittleHorse server (tier 2)'))
      lines.push('')
      integration.sort((a, b) => b[1].passed + b[1].todo - (a[1].passed + a[1].todo))
      let verified = 0
      let todo = 0
      let failed = 0
      for (const [key, g] of integration) {
        verified += g.passed
        todo += g.todo
        failed += g.failed
        const total = g.passed + g.todo + g.failed
        const pct = total === 0 ? 0 : Math.round((g.passed / total) * 100)
        const failedPart = g.failed > 0 ? red(`  ${g.failed} failed`) : ''
        const todoPart = g.todo > 0 ? yellow(`${padStart(g.todo, 3)} todo`) : dim('        ')
        lines.push(
          `  ${pad(AREA_LABELS[key] || key, labelWidth)}${green(padStart(g.passed, 4) + ' verified')}  ${todoPart}  ` +
            `${bar(g.passed, total)} ${padStart(pct + '%', 4)}${failedPart}`
        )
      }
      const grandTotal = verified + todo + failed
      const pct = grandTotal === 0 ? 0 : Math.round((verified / grandTotal) * 100)
      lines.push('')
      lines.push(
        `  ${pad('against a real server', labelWidth)}${green(padStart(verified, 4) + ' verified')}  ` +
          `${todo > 0 ? yellow(padStart(todo, 3) + ' todo') : dim('        ')}  ${bold(pct + '% covered')}` +
          (failed > 0 ? red(`  ${failed} FAILED`) : '')
      )
    }

    if (support.length > 0) {
      lines.push('')
      lines.push(bold('  Supporting suites'))
      lines.push('')
      support.sort((a, b) => b[1].passed - a[1].passed)
      for (const [key, g] of support) {
        const failedPart = g.failed > 0 ? red(`  ${g.failed} failed`) : ''
        lines.push(
          `  ${pad(SUPPORT_LABELS[key] || key, labelWidth)}${green(padStart(g.passed, 4) + ' passed')}${failedPart}`
        )
      }
    }

    lines.push('')
    if (matrix.length > 0) {
      lines.push(dim('  todo = a Java capability not yet ported/proven. See proposals/sdk-js-parity/'))
      lines.push(dim('  List them: npx jest src/feature-matrix --verbose'))
    } else if (integration.length > 0) {
      lines.push(dim('  Proves the real server accepts and executes what the SDK produces,'))
      lines.push(dim('  which the offline suites cannot. See proposals/sdk-js-parity/'))
    }
    lines.push('')

    console.log(lines.join('\n'))
  }
}

module.exports = MatrixReporter
