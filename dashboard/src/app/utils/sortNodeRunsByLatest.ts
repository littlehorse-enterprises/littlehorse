import { NodeRun } from 'littlehorse-client/proto'

export default function sortNodeRunsByLatest(nodeRuns?: NodeRun[]) {
  return nodeRuns?.sort((a, b) => new Date(b.arrivalTime ?? 0).getTime() - new Date(a.arrivalTime ?? 0).getTime())
}
