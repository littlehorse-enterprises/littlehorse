import { TaskAttempt, VariableValue } from 'littlehorse-client/proto'
import { getVariableValue } from './variables'

export const getAttemptOutput = (output: VariableValue | undefined): string => {
  if (!output?.value || output.value.oneofKind === undefined) return 'No Output'
  // Preserve previous behavior: a present-but-falsy primitive payload (0, false, '')
  // was treated as no output (old check was `!output?.value?.value`).
  const payload = (output.value as Record<string, unknown>)[output.value.oneofKind]
  if (!payload) return 'No Output'
  return getVariableValue(output)
}

export const getAttemptResult = (result: Partial<TaskAttempt>['result']): string => {
  if (!result || result.oneofKind === undefined) return 'No Output'

  if (result.oneofKind === 'output') {
    return getAttemptOutput(result.output)
  }

  const val =
    result.oneofKind === 'error' ? result.error : result.oneofKind === 'exception' ? result.exception : undefined
  return (val && (val.message ?? val.toString())) || JSON.stringify(val) || 'No Output'
}
