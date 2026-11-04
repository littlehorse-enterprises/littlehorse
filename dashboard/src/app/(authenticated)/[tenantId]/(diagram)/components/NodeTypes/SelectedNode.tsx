import { CSSProperties, FC, useEffect, useMemo } from 'react'
import { internalsSymbol, useNodeId, useStoreApi } from 'reactflow'
import { useDiagram } from '../../hooks/useDiagram'

export const SelectedNode: FC = () => {
  const contextNodeId = useNodeId()
  const { setSelectedNode, nodes } = useDiagram()
  const store = useStoreApi()
  const selectedNode = useMemo(
    () => nodes.find(node => node.selected && node.id === contextNodeId),
    [contextNodeId, nodes]
  )
  const selectedNodeId = selectedNode?.id

  useEffect(() => {
    if (selectedNode?.id === contextNodeId) {
      setSelectedNode(selectedNode)
    }
  }, [setSelectedNode, selectedNode, contextNodeId])

  // Both effects below write to the reactflow store, which re-renders every
  // store subscriber. They must not depend on the store's `nodes`/`edges`
  // snapshots (new identities on every write), otherwise they re-trigger
  // themselves in an infinite loop — React 19 aborts with "Maximum update
  // depth exceeded". Key them on the selected node id and read current state
  // from the store at effect time, writing only when something changes.
  useEffect(() => {
    if (!selectedNodeId) return
    const { getNodes, setNodes } = store.getState()
    const currentNodes = getNodes()
    const needsUpdate = currentNodes.some(node => (node.selected ? node.zIndex !== 9999 : node.zIndex !== 1))
    if (needsUpdate) {
      setNodes(currentNodes.map(node => (node.selected ? { ...node, zIndex: 9999 } : { ...node, zIndex: 1 })))
    }
  }, [selectedNodeId, store])

  useEffect(() => {
    const { edges, setEdges } = store.getState()
    const styledEdges = edges.map(edge => {
      const connected = selectedNodeId === edge.source
      return {
        ...edge,
        style: {
          stroke: connected ? 'rgb(59 130 246 / var(--tw-bg-opacity, 1))' : '',
          strokeWidth: connected ? 3 : '',
          strokeDasharray: connected ? '1000' : '',
        },
      }
    })
    const changed = edges.some((edge, i) => {
      const prev = edge.style ?? {}
      const next = styledEdges[i].style
      return (
        prev.stroke !== next.stroke ||
        prev.strokeWidth !== next.strokeWidth ||
        prev.strokeDasharray !== next.strokeDasharray
      )
    })
    if (changed) setEdges(styledEdges)
  }, [selectedNodeId, store])

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
