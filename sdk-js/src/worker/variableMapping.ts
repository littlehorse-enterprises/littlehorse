import { VariableValue } from '../proto/type_definition'
import { VarNameAndVal } from '../proto/task_run'
import { ScheduledTask } from '../proto/service'
import { VariableType } from '../proto/common_enums'
import { objToVarVal, varValToObj } from '../common/serde'

/**
 * Task input/output mapping. The actual JS <-> VariableValue conversion lives
 * in common/serde so the worker and the wfsdk cannot diverge; these are the
 * worker-shaped entry points.
 */

/** Extracts a JS value from a VariableValue. */
export function extractVariableValue(variable: VariableValue | undefined): unknown {
  return varValToObj(variable)
}

/** Extracts a ScheduledTask's inputs as positional task arguments. */
export function extractTaskArgs(task: ScheduledTask): unknown[] {
  return task.variables.map((v: VarNameAndVal) => extractVariableValue(v.value))
}

/** Converts a task's return value to a VariableValue. */
export function toVariableValue(value: unknown, declaredType?: VariableType): VariableValue {
  // the declared type disambiguates what the value alone cannot: JS numbers
  // carry no int/double distinction (Java resolves this via Long vs Double)
  if (declaredType === VariableType.DOUBLE && typeof value === 'number') {
    return { value: { oneofKind: 'double', double: value } }
  }
  if (declaredType === VariableType.INT && (typeof value === 'number' || typeof value === 'bigint')) {
    return { value: { oneofKind: 'int', int: value.toString() } }
  }
  return objToVarVal(value)
}
