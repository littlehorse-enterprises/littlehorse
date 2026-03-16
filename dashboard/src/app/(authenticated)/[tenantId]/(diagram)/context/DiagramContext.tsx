import type { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import { Node as NodeProto, NodeRun, WfRun } from 'littlehorse-client/proto'
import { Dispatch, SetStateAction, createContext } from 'react'
import { Node } from 'reactflow'
import { NodeType } from '../components/NodeTypes/extractNodes'

export type ThreadType = {
  name: string
  number: number
}
export type NodeRunType = Node<NodeProto & { nodeRunsList: NodeRun[] }, NodeType>

export type NodeInContext =
  | Node<NodeProto, NodeType>
  | Node<NodeProto & { nodeRunsList: NodeRun[] }, NodeType>
  | undefined

type DiagramContextType = {
  thread: ThreadType
  setThread: Dispatch<SetStateAction<ThreadType>>
  selectedNode: NodeInContext
  setSelectedNode: Dispatch<SetStateAction<NodeInContext>>
  wfRun?: Omit<WfRun, 'threadRuns'> & { threadRuns: ThreadRunWithNodeRuns[] }
}
export const DiagramContext = createContext<DiagramContextType>({
  thread: { name: '', number: 0 },
  setThread: () => {},
  selectedNode: undefined,
  setSelectedNode: () => {},
})

export const DiagramProvider = DiagramContext.Provider
