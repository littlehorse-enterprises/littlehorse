import { Position } from 'reactflow'

export type HandleSide = 'NORTH' | 'SOUTH' | 'EAST' | 'WEST'

export type NopHandleConfig = {
  id: string
  position: Position
  side: HandleSide
  offset: number
}

const positionToSide = (position: Position): HandleSide => {
  switch (position) {
    case Position.Top:
      return 'NORTH'
    case Position.Bottom:
      return 'SOUTH'
    case Position.Left:
      return 'WEST'
    case Position.Right:
    default:
      return 'EAST'
  }
}

type OutgoingEdge = { sinkNodeName: string }

export const getNopSourceHandleConfigs = (outgoingEdges: OutgoingEdge[] = []): NopHandleConfig[] => {
  const cycleEdges = outgoingEdges.filter(edge => edge.sinkNodeName.startsWith('cycle-'))
  const regularEdges = outgoingEdges.filter(edge => !edge.sinkNodeName.startsWith('cycle-'))
  const hasCycle = cycleEdges.length > 0
  const regularCount = regularEdges.length
  const totalCount = outgoingEdges.length

  const configs: Array<{ id: string; position: Position }> = []

  if (hasCycle) {
    if (regularCount === 1) {
      configs.push({ id: 'source-0', position: Position.Right }, { id: 'source-1', position: Position.Bottom })
    } else if (regularCount === 2) {
      configs.push(
        { id: 'source-0', position: Position.Top },
        { id: 'source-1', position: Position.Top },
        { id: 'source-2', position: Position.Bottom }
      )
    } else {
      const isOdd = regularCount % 2 !== 0
      if (isOdd) {
        const halfCount = Math.floor(regularCount / 2)
        for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${i}`, position: Position.Top })
        configs.push({ id: `source-${halfCount}`, position: Position.Right })
        configs.push({ id: `source-${regularCount}`, position: Position.Bottom })
      } else {
        const halfCount = regularCount / 2
        for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${i}`, position: Position.Top })
        for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${halfCount + i}`, position: Position.Right })
        configs.push({ id: `source-${regularCount}`, position: Position.Bottom })
      }
    }
  } else if (totalCount === 1) {
    configs.push({ id: 'source-0', position: Position.Right })
  } else if (totalCount === 2) {
    configs.push({ id: 'source-0', position: Position.Top }, { id: 'source-1', position: Position.Bottom })
  } else if (totalCount === 3) {
    configs.push(
      { id: 'source-0', position: Position.Top },
      { id: 'source-1', position: Position.Right },
      { id: 'source-2', position: Position.Bottom }
    )
  } else {
    const isOdd = totalCount % 2 !== 0
    if (isOdd) {
      const halfCount = Math.floor(totalCount / 2)
      for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${i}`, position: Position.Top })
      configs.push({ id: `source-${halfCount}`, position: Position.Right })
      for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${halfCount + 1 + i}`, position: Position.Bottom })
    } else {
      const halfCount = totalCount / 2
      for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${i}`, position: Position.Top })
      for (let i = 0; i < halfCount; i++) configs.push({ id: `source-${halfCount + i}`, position: Position.Bottom })
    }
  }

  return configs.map(config => ({
    ...config,
    side: positionToSide(config.position),
    offset:
      config.position === Position.Top && configs.filter(c => c.position === Position.Top).length > 1
        ? (configs.filter(c => c.position === Position.Top).indexOf(config) + 1) /
          (configs.filter(c => c.position === Position.Top).length + 1)
        : config.position === Position.Bottom && configs.filter(c => c.position === Position.Bottom).length > 1
          ? (configs.filter(c => c.position === Position.Bottom).indexOf(config) + 1) /
            (configs.filter(c => c.position === Position.Bottom).length + 1)
          : config.position === Position.Right &&
              configs.filter(c => c.position === Position.Right).length > 1
            ? (configs.filter(c => c.position === Position.Right).indexOf(config) + 1) /
              (configs.filter(c => c.position === Position.Right).length + 1)
            : 0.5,
  }))
}
