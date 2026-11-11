import { UserTaskEvent_UTESaved, UserTaskRun, VariableValue } from 'littlehorse-client/proto'

type UserTaskValues = Partial<Pick<UserTaskRun | UserTaskEvent_UTESaved, 'results' | 'output'>>

/**
 * Lists the values entered into a UserTaskRun (or one of its saved-progress events).
 *
 * Struct-backed user tasks carry them as the fields of the `output` Struct and leave the
 * deprecated `results` map empty; legacy field-backed user tasks only populate `results`.
 */
export const getUserTaskResultEntries = (source?: UserTaskValues): [string, VariableValue][] => {
  const output = source?.output?.value
  if (output?.oneofKind === 'struct') {
    return Object.entries(output.struct.struct?.fields ?? {}).flatMap(([name, field]): [string, VariableValue][] =>
      field.value ? [[name, field.value]] : []
    )
  }
  return Object.entries(source?.results ?? {})
}
