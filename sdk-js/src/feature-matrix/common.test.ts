import * as fs from 'fs'
import * as path from 'path'
import { VariableValue } from '../proto/type_definition'
import { VariableType } from '../proto/common_enums'
import { TaskRunId, WfRunId } from '../proto/object_id'
import { Timestamp } from '../proto/google/protobuf/timestamp'
import {
  InputVarSubstitutionError,
  LHError,
  LHMisconfigurationError,
  LHSerdeError,
  lhJsonStringify,
  objToVarVal,
  structToObj,
  taskRunIdToString,
  variableTypeOf,
  varValToObj,
  wfRunIdFromString,
  wfRunIdToString,
} from '../common'
import { LHTaskException, TaskSchemaMismatchError } from '../worker'

/**
 * Feature matrix: common serde and utilities.
 *
 * See sdk-js/PARITY_PLAN.md. This is the proto <-> native value conversion
 * layer everything else depends on (Java: common/LHLibUtil,
 * common/LHVariableMapper, common/exception).
 *
 * Encoding is checked against `golden/fixtures/serde.json`, produced by the
 * Java SDK itself, rather than against our own expectations — two SDKs can
 * each "work" while disagreeing on bytes, and a disagreement silently
 * corrupts data written by one and read by the other. Regenerate with:
 *   ./gradlew :sdk-js-golden-generator:runSerde --args="$(pwd)/sdk-js/golden"
 */

interface SerdeFixture {
  values: Array<{ label: string; encoded: Record<string, unknown> }>
  ids: { simple: string; child: string; grandchild: string; taskRun: string }
}

const fixture: SerdeFixture = JSON.parse(
  fs.readFileSync(path.resolve(__dirname, '../../golden/fixtures/serde.json'), 'utf-8')
)

/** The Java-produced encoding for a fixture label, as proto JSON. */
function javaEncoding(label: string): Record<string, unknown> {
  const entry = fixture.values.find(v => v.label === label)
  if (entry === undefined) throw new Error(`No fixture case labelled '${label}'`)
  return entry.encoded
}

/** Asserts our encoding of `value` matches Java's for that fixture label. */
function expectMatchesJava(label: string, value: unknown): void {
  expect(VariableValue.toJson(objToVarVal(value))).toEqual(javaEncoding(label))
}

describe('common', () => {
  describe('value serde', () => {
    test('convert native values to VariableValue for every VariableType (STR, INT, DOUBLE, BOOL, BYTES, TIMESTAMP, JSON_OBJ, JSON_ARR, WF_RUN_ID) — Java: LHLibUtil#objToVarVal', () => {
      expectMatchesJava('str', 'hello')
      expectMatchesJava('str-empty', '')
      expectMatchesJava('str-unicode', 'héllo → 世界')
      expectMatchesJava('int-long', 42)
      expectMatchesJava('int-zero', 0)
      expectMatchesJava('int-negative', -7)
      expectMatchesJava('int-large', 9007199254740991)
      expectMatchesJava('double', 1.5)
      expectMatchesJava('double-negative', -0.25)
      expectMatchesJava('bool-true', true)
      expectMatchesJava('bool-false', false)
      expectMatchesJava('bytes', new Uint8Array([1, 2, 3, 255]))
      expectMatchesJava('timestamp-epoch', new Date(0))
      expectMatchesJava('timestamp', new Date(1780000000123))
      expectMatchesJava('json-arr', [1, 2, 3])
      expectMatchesJava('json-arr-empty', [])
      expectMatchesJava('json-arr-mixed', ['a', 1, true])
      expectMatchesJava('null', null)
      expect(VariableValue.toJson(objToVarVal(undefined))).toEqual(javaEncoding('null'))

      // WF_RUN_ID is deliberately not inferred from shape — see the note in
      // common/serde.ts. An explicit VariableValue passes straight through.
      const wfRunId = VariableValue.create({ value: { oneofKind: 'wfRunId', wfRunId: { id: 'wf-1' } } })
      expect(objToVarVal(wfRunId)).toEqual(wfRunId)
      expect(variableTypeOf(wfRunId)).toBe(VariableType.WF_RUN_ID)

      // A plain object that merely looks like an id must stay JSON, with all
      // its fields intact.
      expect(varValToObj(objToVarVal({ id: 'abc', name: 'x' }))).toEqual({ id: 'abc', name: 'x' })
    })

    test('convert VariableValue back to native values for every VariableType — Java: LHLibUtil#varValToObj', () => {
      const roundTrip = (value: unknown) => varValToObj(objToVarVal(value))

      expect(roundTrip('hello')).toBe('hello')
      expect(roundTrip(42)).toBe(42)
      expect(roundTrip(-0.25)).toBe(-0.25)
      expect(roundTrip(true)).toBe(true)
      expect(roundTrip(new Uint8Array([1, 2]))).toEqual(new Uint8Array([1, 2]))
      expect(roundTrip(new Date(1780000000123))).toEqual(new Date(1780000000123))
      expect(roundTrip({ a: 1, b: [2, 3] })).toEqual({ a: 1, b: [2, 3] })
      expect(roundTrip([1, 'two', false])).toEqual([1, 'two', false])
      expect(roundTrip(null)).toBeUndefined()

      // Reading each VariableType the server can send, decoded from Java's
      // own encodings rather than ones we constructed.
      expect(varValToObj(VariableValue.fromJson(javaEncoding('int-large')))).toBe(9007199254740991)
      expect(varValToObj(VariableValue.fromJson(javaEncoding('timestamp')))).toEqual(new Date(1780000000123))
      expect(varValToObj(VariableValue.fromJson(javaEncoding('json-obj')))).toEqual({
        zebra: 'last-alphabetically',
        alpha: 1,
        nested: { inner: true },
      })

      // Malformed JSON from the server is a typed serde failure, not a crash.
      expect(() => varValToObj(VariableValue.create({ value: { oneofKind: 'jsonObj', jsonObj: '{oops' } }))).toThrow(
        LHSerdeError
      )
    })

    test('round-trip struct values against a StructDef — Java: LHVariableMapper/struct serde', () => {
      const struct = VariableValue.create({
        value: {
          oneofKind: 'struct',
          struct: {
            structDefId: { name: 'customer-struct', version: 0 },
            struct: {
              fields: {
                name: { value: { value: { oneofKind: 'str', str: 'Ada' } } },
                age: { value: { value: { oneofKind: 'int', int: '36' } } },
                address: {
                  value: {
                    value: {
                      oneofKind: 'struct',
                      struct: {
                        structDefId: { name: 'address-struct', version: 0 },
                        struct: { fields: { city: { value: { value: { oneofKind: 'str', str: 'London' } } } } },
                      },
                    },
                  },
                },
              },
            },
          },
        },
      })

      // Nested structs decode recursively into plain objects.
      expect(varValToObj(struct)).toEqual({ name: 'Ada', age: 36, address: { city: 'London' } })
      if (struct.value.oneofKind !== 'struct') throw new Error('expected a struct')
      expect(structToObj(struct.value.struct)).toEqual({ name: 'Ada', age: 36, address: { city: 'London' } })
    })

    test('serialize JSON values identically to the Java SDK (field ordering, null handling) — Java: LHLibUtil JSON serde', () => {
      // Insertion order is preserved, not sorted.
      expectMatchesJava('json-obj', {
        zebra: 'last-alphabetically',
        alpha: 1,
        nested: { inner: true },
      })
      expectMatchesJava('json-obj-empty', {})

      // Java drops null object fields — including nested ones — but keeps
      // nulls inside arrays. Plain JSON.stringify does neither, which is why
      // lhJsonStringify exists.
      expectMatchesJava('json-obj-null-field', { present: 'yes', absent: null })
      expectMatchesJava('json-obj-nested-null', { outer: { kept: 1, dropped: null } })
      expectMatchesJava('json-obj-all-null', { only: null })
      expectMatchesJava('json-arr-with-null', [1, null, 2])

      expect(lhJsonStringify({ a: 1, b: null })).toBe('{"a":1}')
      expect(lhJsonStringify([1, null])).toBe('[1,null]')
      expect(JSON.stringify({ a: 1, b: null })).toBe('{"a":1,"b":null}') // the difference
    })

    test('convert timestamps with the same precision and encoding as Java — Java: LHLibUtil timestamp handling', () => {
      // Millisecond precision, RFC-3339 with a trailing Z; whole seconds omit
      // the fractional part, matching Java's rendering exactly.
      expectMatchesJava('timestamp-epoch', new Date(0))
      expectMatchesJava('timestamp', new Date(1780000000123))
      expect(javaEncoding('timestamp-epoch')).toEqual({ utcTimestamp: '1970-01-01T00:00:00Z' })
      expect(javaEncoding('timestamp')).toEqual({ utcTimestamp: '2026-05-28T20:26:40.123Z' })

      // No drift through a full round trip.
      const when = new Date('2026-07-28T12:34:56.789Z')
      expect(varValToObj(objToVarVal(when))).toEqual(when)
      const encoded = objToVarVal(when)
      if (encoded.value.oneofKind !== 'utcTimestamp') throw new Error('expected a timestamp')
      expect(Timestamp.toDate(encoded.value.utcTimestamp).getTime()).toBe(when.getTime())
    })

    test('format and parse WfRunId/composite ids identically to Java — Java: LHLibUtil id helpers', () => {
      const parent = WfRunId.create({ id: 'parent-1' })
      const child = WfRunId.create({ id: 'child-1', parentWfRunId: parent })
      const grandchild = WfRunId.create({ id: 'gc-1', parentWfRunId: child })

      expect(wfRunIdToString(parent)).toBe(fixture.ids.simple)
      expect(wfRunIdToString(child)).toBe(fixture.ids.child)
      expect(wfRunIdToString(grandchild)).toBe(fixture.ids.grandchild)
      expect(taskRunIdToString(TaskRunId.create({ wfRunId: child, taskGuid: 'guid-9' }))).toBe(fixture.ids.taskRun)

      // Parsing inverts formatting, including the nested parent chain.
      expect(wfRunIdFromString(fixture.ids.simple)).toEqual(parent)
      expect(wfRunIdFromString(fixture.ids.child)).toEqual(child)
      expect(wfRunIdFromString(fixture.ids.grandchild)).toEqual(grandchild)
    })
  })

  describe('errors', () => {
    test('raise typed errors matching the Java exception taxonomy (serde, misconfiguration, task exception) — Java: common/exception', () => {
      // Callers branch on the kind of failure, so each kind is its own class
      // under a shared base.
      for (const error of [
        new LHSerdeError('bad value'),
        new LHMisconfigurationError('bad setup'),
        new InputVarSubstitutionError('bad inputs'),
      ]) {
        expect(error).toBeInstanceOf(LHError)
        expect(error).toBeInstanceOf(Error)
        expect(error.name).toBe(error.constructor.name)
        expect(error.message.length).toBeGreaterThan(0)
      }

      // Distinguishable from one another, not just from Error.
      expect(new LHSerdeError('x')).not.toBeInstanceOf(LHMisconfigurationError)

      // A business exception thrown by a task carries a name and content, and
      // is deliberately NOT an LHError: it is the user's domain failure, not
      // an SDK fault.
      const taskException = new LHTaskException('out-of-stock', 'no widgets', { sku: 'W-1' })
      expect(taskException).toBeInstanceOf(Error)
      expect(taskException).not.toBeInstanceOf(LHError)
      expect(taskException.name).toBe('out-of-stock')
      expect(taskException.content).toEqual({ sku: 'W-1' })

      // A worker/TaskDef signature mismatch is its own type too.
      expect(new TaskSchemaMismatchError('mismatch')).toBeInstanceOf(Error)
      expect(new TaskSchemaMismatchError('mismatch').name).toBe('TaskSchemaMismatchError')
    })
  })
})
