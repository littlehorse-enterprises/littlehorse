import { expectMatchesGolden } from './golden'
import { referenceWorkflows } from './referenceWorkflows'

/**
 * Golden conformance tests: each reference workflow (referenceWorkflows.ts)
 * is the TypeScript equivalent of a Java workflow in sdk-js/golden/generator.
 * The compiled PutWfSpecRequest must match the Java SDK's output exactly.
 */
describe('wfsdk golden conformance', () => {
  test.each(Object.keys(referenceWorkflows))('%s', name => {
    expectMatchesGolden(referenceWorkflows[name]().compileWorkflow(), name)
  })
})
