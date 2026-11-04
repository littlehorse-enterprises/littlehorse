/**
 * serde cases: typed inputs (rules.md S1) through the SDK's own converter —
 * the one the task worker uses, so this exam guards the worker path too.
 */
import { readFileSync } from 'node:fs'
import { join } from 'node:path'
import { toVariableValue } from '../../dist/worker/variableMapping'
import { VariableValue } from '../../dist/proto/type_definition'
import { VariableType } from '../../dist/proto/common_enums'

/** Maps a typed input (rules.md S1) to a native value. */
function nativeFromTyped(type: string, value: string | undefined): unknown {
  switch (type) {
    case 'str':
      return value
    case 'int':
      // BigInt keeps int64 inputs exact beyond 2^53
      return BigInt(value ?? '0')
    case 'double':
      return Number(value)
    case 'bool':
      return value === 'true'
    case 'bytes':
      return Buffer.from(value ?? '', 'base64')
    case 'timestamp':
      return new Date(Number(value))
    case 'json-obj':
    case 'json-arr':
      return JSON.parse(value ?? '')
    case 'null':
      return null
    default:
      throw new Error(`unknown input type: ${type}`)
  }
}

const DECLARED: Record<string, VariableType> = { int: VariableType.INT, double: VariableType.DOUBLE }

export function caseIds(): string[] {
  // runtime __dirname is conformance/build/ → repo root is three up
  const manifestPath = join(__dirname, '..', '..', '..', 'sdk-conformance', 'areas', 'serde', 'manifest.json')
  const manifest = JSON.parse(readFileSync(manifestPath, 'utf8')) as { cases: Array<{ id: string }> }
  return manifest.cases.map(c => c.id)
}

export function convert(type: string, value: string | undefined): string {
  const converted = toVariableValue(nativeFromTyped(type, value), DECLARED[type])
  return JSON.stringify(VariableValue.toJson(converted, { emitDefaultValues: true }), null, 2)
}
