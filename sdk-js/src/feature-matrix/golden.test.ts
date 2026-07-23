import { PutWfSpecRequest } from '../proto/service'
import { listGoldens, loadGolden } from './golden'

/**
 * Harness self-test: proves the golden pipeline works end-to-end before the
 * wfsdk exists. Every Java-generated golden must parse into our generated
 * PutWfSpecRequest with no unknown fields (i.e. our proto codegen is in sync
 * with the server's API version) and survive a serde round-trip.
 */
describe('golden harness', () => {
  const goldens = listGoldens()

  test('golden files exist', () => {
    expect(goldens.length).toBeGreaterThan(0)
  })

  test.each(goldens)('golden "%s" parses with no unknown fields', name => {
    const wfSpec = loadGolden(name)
    expect(wfSpec.name).toBe(`golden-${name}`)
    expect(Object.keys(wfSpec.threadSpecs)).toContain(wfSpec.entrypointThreadName || 'entrypoint')
  })

  test.each(goldens)('golden "%s" survives a proto JSON round-trip', name => {
    const wfSpec = loadGolden(name)
    const roundTripped = PutWfSpecRequest.fromJsonString(JSON.stringify(PutWfSpecRequest.toJson(wfSpec)))
    expect(PutWfSpecRequest.equals(wfSpec, roundTripped)).toBe(true)
  })
})
