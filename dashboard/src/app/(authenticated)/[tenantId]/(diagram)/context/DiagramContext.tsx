import { Node as NodeProto } from 'littlehorse-client/proto'
import { Dispatch, SetStateAction, createContext } from 'react'
import { Node } from 'reactflow'
import { NodeType } from '../components/NodeTypes/extractNodes'

export type ThreadType = {
  name: string
  number: number
}
export type NodeInContext = Node<NodeProto, NodeType> | undefined

type DiagramContextType = {
  thread: ThreadType
  setThread: Dispatch<SetStateAction<ThreadType>>
  selectedNode: NodeInContext
  setSelectedNode: Dispatch<SetStateAction<NodeInContext>>
}
export const DiagramContext = createContext<DiagramContextType>({
  thread: { name: '', number: 0 },
  setThread: () => {},
  selectedNode: undefined,
  setSelectedNode: () => {},
})

export const DiagramProvider = DiagramContext.Provider
