import { WfRunId } from 'littlehorse-client/proto'

export const wfRunIdToPath = (wfRunId: WfRunId) => {
  const ids = []
  let current: WfRunId | undefined = wfRunId
  while (current) {
    ids.push(current.id)
    current = current.parentWfRunId
  }
  return ids.reverse().join('/')
}

export const flattenWfRunId = (wfRunId: WfRunId): string => {
  if (!wfRunId.parentWfRunId) return wfRunId.id
  return flattenWfRunId(wfRunId.parentWfRunId) + '_' + wfRunId.id
}

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

