import { Position } from 'reactflow'
import { NodeProps } from '..'

export const nopHandles = (node: NodeProps) => {
  const { outgoingEdges } = node.data
  const getLevel = (level: string) => parseInt(level.split('-')[0])

  // Define positions for forward and backward edges
  const forwardPositions = [Position.Top, Position.Bottom]
  const backwardPositions = [Position.Bottom, Position.Top]

  let forwardCounter = 0,
    backwardCounter = 0

  return outgoingEdges.map((edge: { sinkNodeName: string }) => {
    // check for forward edges or bacward changes
    const isForward = getLevel(node.id) < getLevel(edge.sinkNodeName)
    const isSingleEdge = outgoingEdges.length === 1

    // Determine position based on edge direction and count
    return isForward
      ? isSingleEdge
        ? Position.Right
        : forwardPositions[forwardCounter++]
      : isSingleEdge
        ? Position.Bottom
        : backwardPositions[backwardCounter++]
  })
}
