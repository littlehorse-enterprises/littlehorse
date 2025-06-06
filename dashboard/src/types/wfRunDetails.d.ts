import { WfRun, NodeRun, Variable, ThreadRun } from "littlehorse-client/proto"

export type ThreadRunWithNodeRuns = ThreadRun & { nodeRuns: NodeRun[] }

export type WfRunDetails = {
  wfRun: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  nodeRuns: NodeRun[]
  variables: Variable[]
} 