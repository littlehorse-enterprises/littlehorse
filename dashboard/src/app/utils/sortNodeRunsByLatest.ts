import { NodeRun } from 'littlehorse-client/proto'

export const sortNodeRunsByLatest = <T extends NodeRun>(nodeRuns: T[] = []): T[] => {
  return nodeRuns.sort((a, b) => new Date(b.arrivalTime ?? 0).getTime() - new Date(a.arrivalTime ?? 0).getTime())
}
