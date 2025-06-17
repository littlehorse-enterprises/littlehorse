import { type Node, type Edge, Position } from '@xyflow/react'
import ELK from 'elkjs/lib/elk.bundled.js'

// Define node dimensions
export const nodeWidth = 172
export const nodeHeight = 36

// Initialize ELK instance
const elk = new ELK()

// Layout directions (keeping for potential future use)
export const LayoutDirection = {
  TOP_TO_BOTTOM: 'DOWN',
  LEFT_TO_RIGHT: 'RIGHT',
  BOTTOM_TO_TOP: 'UP',
  RIGHT_TO_LEFT: 'LEFT',
} as const

export type LayoutDirectionType = (typeof LayoutDirection)[keyof typeof LayoutDirection]

// Layout options interface - removed direction since it's always horizontal
export interface LayoutOptions {
  algorithm?: string
  nodeSpacing?: string
  layerSpacing?: string
  customOptions?: Record<string, string>
}

export const getLayoutedElements = async (
  nodes: Node[],
  edges: Edge[],
  options: LayoutOptions = {}
): Promise<{ nodes: Node[]; edges: Edge[] }> => {
  if (!nodes.length) return { nodes, edges }

  const { algorithm = 'layered', nodeSpacing = '50', layerSpacing = '25', customOptions = {} } = options

  // Direction is always horizontal (LEFT_TO_RIGHT)
  const direction = LayoutDirection.LEFT_TO_RIGHT

  const layoutOptions = {
    'elk.algorithm': algorithm,
    'elk.direction': direction,
    'elk.spacing.nodeNode': nodeSpacing,
    'elk.layered.spacing.nodeNodeBetweenLayers': layerSpacing,
    ...customOptions,
  }

  const graph = {
    id: 'root',
    layoutOptions,
    children: nodes.map(node => ({
      ...node,
      width: nodeWidth,
      height: nodeHeight,
    })),
    edges: edges.map(edge => ({
      id: edge.id,
      sources: [edge.source],
      targets: [edge.target],
    })),
  }

  try {
    const layoutedGraph = await elk.layout(graph)

    const layoutedNodes: Node[] =
      layoutedGraph.children?.map(node => ({
        id: node.id,
        type: 'node',
        data: nodes.find(n => n.id === node.id)?.data || { label: node.id },
        targetPosition: Position.Left,
        sourcePosition: Position.Right,
        position: { x: node.x || 0, y: node.y || 0 },
      })) || []

    return { nodes: layoutedNodes, edges }
  } catch (error) {
    console.error('Layout error:', error)
    return { nodes, edges }
  }
}
