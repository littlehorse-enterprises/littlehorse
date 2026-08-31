/**
 * CLI dispatch (contract: conformance/README.md). Build first:
 *   npm --prefix sdk-js run build && npm --prefix sdk-js run build:conformance
 */
import * as wfsdk from './wfsdkArea'
import * as serde from './serdeArea'

const args = process.argv.slice(2)
if (args[0] === 'list') {
  for (const id of wfsdk.caseIds()) console.log(id)
  for (const id of serde.caseIds()) console.log(id)
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
console.error('usage: testee list | compile --case ID --variant base|feature | convert --type T [--value V]')
process.exit(2)
