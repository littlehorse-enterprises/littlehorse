'use client'

import NodeComponent from '@/components/diagram/node'
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
import { CustomNode, CustomEdge } from '@/types'
import { CustomEdgeComponent } from './edge'
import dagre from '@dagrejs/dagre'

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

const dagreGraph = new dagre.graphlib.Graph()

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

function getLayoutedElements(nodes: CustomNode[], edges: CustomEdge[]) {
  // #region GraphSetup
  dagreGraph.setGraph({ rankdir: 'LR', align: 'UR' })

  nodes.forEach(node => {
    dagreGraph.setNode(node.id, { width: 100, height: 100 })
  })
  edges.forEach(edge => {
    dagreGraph.setEdge(edge.source, edge.target, { width: edge.label ? 200 : 10 })
  })

  dagre.layout(dagreGraph)
  // #endregion

  const layoutedNodes = nodes.map(node => {
    const dagreNode = dagreGraph.node(node.id)

    return {
      ...node,
      position: { x: dagreNode.x - 100 / 2, y: dagreNode.y - 100 / 2 },
      layouted: true,
    }
  })

  return { nodes: layoutedNodes, edges }
}
