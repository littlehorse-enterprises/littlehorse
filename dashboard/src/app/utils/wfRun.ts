import { lHStatusFromJSON, WfRunId } from 'littlehorse-client/proto'

/**
 * Converts a WfRunId to a path string by concatenating its IDs.
 * The path is constructed from the root WfRunId to the current one.
 *
 * @param wfRunId - The WfRunId to convert to a path.
 * @returns A string representing the path of WfRunIds.
 */
export const wfRunIdToPath = (wfRunId: WfRunId) => {
  const ids = []
  let current: WfRunId | undefined = wfRunId
  while (current) {
    ids.push(current.id)
    current = current.parentWfRunId
  }
  return ids.reverse().join('/')
}

/**
 * Converts a list of strings into a WfRunId
 * @param ids list of strings
 * @returns WfRunId or error
 */
export const wfRunIdFromList = (ids: string[]): WfRunId => {
  if (ids.length === 0) throw new Error('ids are empty')

  return ids.reverse().reduce<WfRunId | undefined>((parentWfRunId, id) => ({ id, parentWfRunId }), undefined) as WfRunId
}

/**
 * Flattens a WfRunId into a string by concatenating its IDs with underscores.
 * This is useful for creating unique identifiers for WfRunIds in a flat structure.
 *
 * @param wfRunId - The WfRunId to flatten.
 * @returns A string representing the flattened WfRunId.
 */
export const flattenWfRunId = (wfRunId: WfRunId): string => {
  if (!wfRunId.parentWfRunId) return wfRunId.id
  return flattenWfRunId(wfRunId.parentWfRunId) + '_' + wfRunId.id
}

/**
 * Converts a flattened WfRunId string back to a WfRunId object.
 * This is the reverse operation of flattenWfRunId.
 *
 * @param flattenedId - The flattened WfRunId string to convert.
 * @returns A WfRunId object reconstructed from the flattened string.
 */
export const wfRunIdFromFlattenedId = (flattenedId: string): WfRunId => {
  const ids = flattenedId.split('_')

  return ids.reduce<WfRunId | undefined>(
    (parentWfRunId, currentId) => ({
      id: currentId,
      parentWfRunId,
    }),
    undefined
  )!
}

/**
 * Converts a status string to an LHStatus enum value.
 * Returns undefined if the status is null or empty.
 *
 * @param status - The status string to convert.
 * @returns The LHStatus enum value or undefined.
 */
export const getStatus = (status: string | null) => {
  if (!status) return undefined
  return lHStatusFromJSON(status)
}
