'use client'

import React, { useCallback, useEffect } from 'react'
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  type NodeTypes,
  useNodesState,
  useEdgesState,
  type Node,
  type Edge,
  BackgroundVariant,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { getLayoutedElements } from '@/utils/ui/layout-utils'
import { useSelection } from '../context/selection-context'
import NodeComponent from '@/components/diagram/node'

// Define custom node types
const nodeTypes: NodeTypes = {
  node: NodeComponent,
}

interface WorkflowDiagramProps {
  className?: string
  nodes: Node[]
  edges: Edge[]
}

export default function WorkflowDiagram({
  className = '',
  nodes,
  edges,
}: WorkflowDiagramProps) {
  const [nodesState, setNodesState, onNodesStateChange] = useNodesState<Node>([])
  const [edgesState, setEdgesState, onEdgesStateChange] = useEdgesState<Edge>([])
  const { selectedId, setSelectedId } = useSelection()

  useEffect(() => {
    const applyLayout = async () => {
      const { nodes: layoutedNodes, edges: layoutedEdges } = await getLayoutedElements(nodes, edges)
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
    (event: React.MouseEvent, node: Node) => {
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
        defaultEdgeOptions={{ type: 'step' }}
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
