import { Node as LHNode, NodeRun, Edge as LHEdge } from 'littlehorse-client/proto'
import { Node as RFNode } from '@xyflow/react'
import { Edge as RFEdge } from '@xyflow/react'

import { NodeType } from '@/utils/data/node'

export type NodeData = { node: LHNode; nodeRun?: NodeRun; type: NodeType; label: string }
export type CustomNode = RFNode<NodeData>

export type EdgeData = { edge: LHEdge }
export type CustomEdge = RFEdge<EdgeData>
