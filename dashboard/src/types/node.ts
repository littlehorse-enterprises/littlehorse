import { Node as LHNode, NodeRun, Edge as LHEdge } from 'littlehorse-client/proto'
import { Node as RFNode } from '@xyflow/react'
import { Edge as RFEdge } from '@xyflow/react'
import { OneOfCases } from '@/utils/data/oneof-utils'

export type NodeData = {
  node: LHNode
  nodeRun?: NodeRun
  type: OneOfCases<LHNode['node']>
  label: string
}
export type CustomNode = RFNode<NodeData>

export type EdgeData = { edge: LHEdge }
export type CustomEdge = RFEdge<EdgeData>
