import { CSSProperties, FC, PropsWithChildren, useEffect, useMemo } from 'react'
import { internalsSymbol, useNodeId, useStore } from 'reactflow'

type Props = PropsWithChildren<{}>

export const NodeDetails: FC<Props> = ({ children }) => {
  const contextNodeId = useNodeId()
  const nodes = useStore(state => state.getNodes())
  const setNodes = useStore(state => state.setNodes)
  const selectedNode = useMemo(
    () => nodes.find(node => node.selected && node.id === contextNodeId),
    [contextNodeId, nodes]
  )

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
      <div className="max-w-96 rounded-md bg-white p-2 text-xs">{children}</div>
      <div className="flex items-center justify-center">
        <div className="transform-x-1/2 transform-y-1/2 h-4 w-4 border-[0.5rem] border-transparent border-t-white bg-transparent"></div>
      </div>
    </div>
  )
}
