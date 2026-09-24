/**
 * CLI dispatch (contract: sdk-conformance/README.md). Build first:
 *   npm --prefix sdk-js run build && npm --prefix sdk-js run build:conformance
 */
import { readFileSync } from 'node:fs'
import * as wfsdk from './wfsdkArea'
import * as serde from './serdeArea'
import * as registrations from './registrationsArea'
import * as fuzz from './fuzz'

const args = process.argv.slice(2)
let registrationPending = false
if (args[0] === 'compile-all') {
  const out: Record<string, unknown> = {}
  for (const id of wfsdk.caseIds()) {
    const variants = id === 'workflow-minimal' ? ['feature'] : ['base', 'feature']
    for (const variant of variants) out[`${id}/${variant}`] = JSON.parse(wfsdk.compile(id, variant))
  }
  console.log(JSON.stringify(out))
  process.exit(0)
}
if (args[0] === 'convert-batch') {
  const lines = readFileSync(0, 'utf8').split('\n').filter(Boolean)
  const out: Record<string, unknown> = {}
  for (const line of lines) {
    const req = JSON.parse(line) as { id: string; type: string; value?: string }
    out[req.id] = JSON.parse(serde.convert(req.type, req.value))
  }
  console.log(JSON.stringify(out))
  process.exit(0)
}
if (args[0] === 'registrations' && args[1] === '--case' && args[3] === '--variant') {
  try {
    console.log(registrations.answer(args[2], args[4]))
    process.exit(0)
  } catch (e) {
    console.error((e as Error).message)
    process.exit(2)
  }
}
if (args[0] === 'registrations-all') {
  const out: Record<string, unknown> = {}
  for (const id of registrations.caseIds()) {
    for (const variant of ['base', 'feature']) out[`${id}/${variant}`] = JSON.parse(registrations.answer(id, variant))
  }
  console.log(JSON.stringify(out))
  process.exit(0)
}
if (args[0] === 'fuzz' && args[1] === '--seed' && args[3] === '--ops') {
  const seed = Number(args[2])
  const ops = Number(args[4])
  const out = fuzz.compile(seed, ops)
  if (args[5] === '--register') {
    // keep the process alive for the async registration; the handlers below
    // are the only exits on this path
    registrationPending = true
    fuzz
      .register(seed, ops)
      .then(() => {
        console.log(out)
        process.exit(0)
      })
      .catch(e => {
        console.error(`server rejected fuzz-${seed}: ${(e as Error).message}`)
        process.exit(3)
      })
  } else {
    console.log(out)
    process.exit(0)
  }
}
if (args[0] === 'list') {
  for (const id of wfsdk.caseIds()) console.log(id)
  for (const id of serde.caseIds()) console.log(id)
  for (const id of registrations.caseIds()) console.log(id)
  process.exit(0)
}
if (args[0] === 'compile' && args[1] === '--case' && args[3] === '--variant') {
  try {
    console.log(wfsdk.compile(args[2], args[4]))
    process.exit(0)
  } catch (e) {
    console.error((e as Error).message)
    process.exit(2)
  }
}
if (args[0] === 'convert' && args[1] === '--type') {
  const value = args[3] === '--value' ? args[4] : undefined
  try {
    console.log(serde.convert(args[2], value))
    process.exit(0)
  } catch (e) {
    console.error((e as Error).message)
    process.exit(2)
  }
}
if (!registrationPending) {
  console.error('usage: testee list | compile --case ID --variant base|feature | convert --type T [--value V]')
  process.exit(2)
}
