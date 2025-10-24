import { NodeRun } from 'littlehorse-client/proto'

export const sortNodeRunsByLatest = (nodeRuns: NodeRun[] = []): NodeRun[] => {
  return nodeRuns.sort((a, b) => b.id!.position - a.id!.position)
}
