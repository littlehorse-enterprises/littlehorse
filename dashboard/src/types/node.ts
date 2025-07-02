import { OneOfCases } from '@/types/oneof'
import { Edge as RFEdge, Node as RFNode } from '@xyflow/react'
import { Edge as LHEdge, Node as LHNode, NodeRun } from 'littlehorse-client/proto'

export type NodeData = {
  node: LHNode
  nodeRun?: NodeRun
  type: OneOfCases<LHNode['node']>
  label: string
}
export type CustomNode = RFNode<NodeData>

export type EdgeData = { edge: LHEdge }
export type CustomEdge = RFEdge<EdgeData>
