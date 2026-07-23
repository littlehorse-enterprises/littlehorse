/**
 * Feature matrix: common serde and utilities.
 *
 * See sdk-js/PARITY_PLAN.md. This is the proto <-> native value conversion
 * layer everything else depends on (Java: common/LHLibUtil,
 * common/LHVariableMapper, common/exception). Semantic gotchas discovered
 * here (enum encoding, timestamp precision, JSON canonicalization) must be
 * recorded as comments next to the tests that pin them down.
 */

describe('common', () => {
  describe('value serde', () => {
    test.todo(
      'convert native values to VariableValue for every VariableType (STR, INT, DOUBLE, BOOL, BYTES, TIMESTAMP, JSON_OBJ, JSON_ARR, WF_RUN_ID) — Java: LHLibUtil#objToVarVal'
    )
    test.todo('convert VariableValue back to native values for every VariableType — Java: LHLibUtil#varValToObj')
    test.todo('round-trip struct values against a StructDef — Java: LHVariableMapper/struct serde')
    test.todo(
      'serialize JSON values identically to the Java SDK (field ordering, null handling) — Java: LHLibUtil JSON serde'
    )
    test.todo('convert timestamps with the same precision and encoding as Java — Java: LHLibUtil timestamp handling')
    test.todo('format and parse WfRunId/composite ids identically to Java — Java: LHLibUtil id helpers')
  })

  describe('errors', () => {
    test.todo(
      'raise typed errors matching the Java exception taxonomy (serde, misconfiguration, task exception) — Java: common/exception'
    )
  })
})
