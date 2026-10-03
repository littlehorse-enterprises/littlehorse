import { Position } from 'reactflow'
import { Node } from 'littlehorse-client/proto'

export type NopHandlePlacement = {
  id: string
  position: Position
  pct: number
}

export const nopSourceHandlePlacements = (outgoingEdges: Node['outgoingEdges']): NopHandlePlacement[] => [
  { id: 'source-0', position: Position.Right, pct: 0.5 },
  ...(outgoingEdges.some(edge => edge.sinkNodeName.startsWith('cycle-'))
    ? [{ id: 'source-loop', position: Position.Bottom, pct: 0.5 }]
    : []),
]
