'use client'

import NodeComponent from '@/components/diagram/node'
import { getLayoutedElements } from '@/utils/ui/layout-utils'
import {
  Background,
  BackgroundVariant,
  Controls,
  EdgeTypes,
  MiniMap,
  ReactFlow,
  useEdgesState,
  useNodesState,
  type NodeTypes,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import React, { useCallback, useEffect } from 'react'
import { useNodeSelection } from '../context/selection-context'
import { CustomNode, CustomEdge } from '@/types/node'
import { CustomEdgeComponent } from './edge'

// Define custom node types
const nodeTypes: NodeTypes = {
  node: NodeComponent,
}

const edgeTypes: EdgeTypes = {
  edge: CustomEdgeComponent,
}

interface WorkflowDiagramProps {
  className?: string
  nodes: CustomNode[]
  edges: CustomEdge[]
}

export default function WorkflowDiagram({ className = '', nodes, edges }: WorkflowDiagramProps) {
  const [nodesState, setNodesState, onNodesStateChange] = useNodesState<CustomNode>([])
  const [edgesState, setEdgesState, onEdgesStateChange] = useEdgesState<CustomEdge>([])
  const { selectedId, setSelectedId } = useNodeSelection()

  useEffect(() => {
    const applyLayout = () => {
      const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(nodes, edges)
      setNodesState(layoutedNodes)
      setEdgesState(layoutedEdges)
    }

    applyLayout()
  }, [nodes, edges, setNodesState, setEdgesState])

  // Update node selection when selectedId changes
  useEffect(() => {
    setNodesState(nds =>
      nds.map(node => ({
        ...node,
        selected: node.id === selectedId,
      }))
    )
  }, [selectedId, setNodesState])

  const handleNodeClick = useCallback(
    (event: React.MouseEvent, node: CustomNode) => {
      setSelectedId(node.id)
    },
    [setSelectedId]
  )

  return (
    <div className={`h-full w-full ${className}`}>
      <ReactFlow
        nodes={nodesState}
        edges={edgesState}
        onNodesChange={onNodesStateChange}
        onEdgesChange={onEdgesStateChange}
        onNodeClick={handleNodeClick}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        defaultEdgeOptions={{ type: 'edge', selectable: false, animated: true }}
        fitView
        attributionPosition="bottom-right"
        nodesDraggable={false}
        nodesConnectable={false}
        connectOnClick={false}
        onPaneClick={() => setSelectedId(null)}
      >
        <Controls />
        <MiniMap />
        <Background color="#aaa" gap={16} size={1} variant={BackgroundVariant.Dots} />
      </ReactFlow>
    </div>
  )
}
