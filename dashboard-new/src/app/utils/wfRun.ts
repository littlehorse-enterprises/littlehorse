import { WfRunId } from 'littlehorse-client/dist/proto/object_id'

export const concatWfRunIds = (wfRunId: WfRunId) => {
  const ids = []
  let current: WfRunId | undefined = wfRunId
  while (current) {
    ids.push(current.id)
    current = current.parentWfRunId
  }
  return ids.reverse().join('/')
}
