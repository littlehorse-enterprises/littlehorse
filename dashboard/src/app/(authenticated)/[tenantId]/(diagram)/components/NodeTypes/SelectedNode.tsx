import { CSSProperties, FC, useCallback, useEffect, useMemo } from 'react'
import { internalsSymbol, useNodeId } from 'reactflow'
import { useDiagram } from '../../hooks/useDiagram'

export const SelectedNode: FC = () => {
  const contextNodeId = useNodeId()
  const { setSelectedNode, nodes, setNodes, edges, setEdges } = useDiagram()
  const selectedNode = useMemo(
    () => nodes.find(node => node.selected && node.id === contextNodeId),
    [contextNodeId, nodes]
  )

  useEffect(() => {
    setSelectedNode(selectedNode)
  }, [setSelectedNode, selectedNode])

  useEffect(() => {
    if (selectedNode && selectedNode.zIndex !== 9999) {
      setNodes(
        nodes.map(node => {
          if (node.selected) {
            return { ...node, zIndex: 9999 }
          } else {
            return { ...node, zIndex: 1 }
          }
        })
      )
    }
  }, [nodes, selectedNode, setNodes])

  useEffect(() => {
    updateEdges()
  }, [selectedNode])

  const updateEdges = useCallback(() => {
    setEdges(
      edges.map(edge => {
        const connected = selectedNode?.id === edge.source
        return {
          ...edge,
          style: {
            stroke: connected ? 'rgb(59 130 246 / var(--tw-bg-opacity, 1))' : '',
            strokeWidth: connected ? 3 : '',
            strokeDasharray: connected ? '1000' : '',
          },
        }
      })
    )
  }, [setEdges, edges, selectedNode])

  const zIndex: number = Math.max(...nodes.map(node => (node[internalsSymbol]?.z || 1) + 10))
  if (!selectedNode) {
    return null
  }

  const wrapperStyle: CSSProperties = {
    position: 'absolute',
    bottom: selectedNode.height!,
    transform: `translate(${selectedNode.width! / 2}px, 0px) translate(-50%, 0%)`,
    zIndex,
  }

  return (
    <div style={wrapperStyle} className="flex flex-col justify-center drop-shadow">
      <div className="transform-x-1/2 transform-y-1/2 h-4 w-4 animate-bounce border-[0.5rem] border-transparent border-t-blue-500 bg-transparent"></div>
    </div>
  )
}
