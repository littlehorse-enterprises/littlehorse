import { VariableValue } from '../proto/type_definition'
import { VarNameAndVal } from '../proto/task_run'
import { ScheduledTask } from '../proto/service'
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
export function toVariableValue(value: unknown): VariableValue {
  return objToVarVal(value)
}
