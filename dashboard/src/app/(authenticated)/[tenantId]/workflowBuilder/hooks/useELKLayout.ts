import { useCallback, useMemo } from 'react'
import ELK from 'elkjs/lib/elk.bundled.js'
import type { Node as ReactFlowNode, Edge as ReactFlowEdge } from 'reactflow'
import type { LayoutResult, LayoutDirection } from '../types'
import { DEFAULT_ELK_OPTIONS } from '../lib/constants'

interface UseELKLayoutResult {
  getLayoutedElements: (
    nodes: ReactFlowNode[],
    edges: ReactFlowEdge[],
    direction: LayoutDirection
  ) => Promise<LayoutResult>
}

export function useELKLayout(): UseELKLayoutResult {
  const elk = useMemo(() => new ELK(), [])

  const getLayoutedElements = useCallback(
    async (
      nodes: ReactFlowNode[],
      edges: ReactFlowEdge[],
      direction: LayoutDirection = 'DOWN'
    ): Promise<LayoutResult> => {
      const isHorizontal = direction === 'RIGHT'

      const graph = {
        id: 'root',
        layoutOptions: {
          ...DEFAULT_ELK_OPTIONS,
          'elk.direction': direction,
        },
        children: nodes.map(node => ({
          ...node,
          targetPosition: isHorizontal ? 'left' : 'top',
          sourcePosition: isHorizontal ? 'right' : 'bottom',
          width: node.width ?? 150,
          height: node.height ?? 50,
        })),
        edges: edges.map(edge => ({
          ...edge,
          sources: [edge.source],
          targets: [edge.target],
        })),
      }

      const layoutedGraph = await elk.layout(graph)

      const layoutedNodes = (layoutedGraph.children ?? []).map(elkNode => {
        const originalNode = nodes.find(n => n.id === elkNode.id)

        return {
          ...originalNode,
          id: elkNode.id ?? originalNode?.id ?? '',
          position: {
            x: elkNode.x ?? 0,
            y: elkNode.y ?? 0,
          },
          width: elkNode.width,
          height: elkNode.height,
        } as ReactFlowNode
      })

      return {
        nodes: layoutedNodes,
        edges: layoutedGraph.edges ?? [],
      }
    },
    [elk]
  )

  return { getLayoutedElements }
}
