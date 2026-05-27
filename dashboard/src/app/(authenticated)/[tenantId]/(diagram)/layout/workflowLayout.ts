import ELK, { ElkNode, LayoutOptions } from 'elkjs/lib/elk.bundled.js'
import { Edge, Node } from 'reactflow'
import { Point, routeForwardEdge, pathMidpoint, pointsToPath } from './edgeRouter'
import { getHandleSideAndOffset } from './portMapping'

const elk = new ELK()

const NODE_GAP = 72
const HORIZONTAL_GAP = 120

const LAYOUT_OPTIONS: LayoutOptions = {
  'elk.algorithm': 'layered',
  'elk.direction': 'RIGHT',
  'elk.spacing.nodeNode': `${NODE_GAP}`,
  'elk.layered.spacing.nodeNodeBetweenLayers': `${HORIZONTAL_GAP}`,
  'elk.spacing.edgeEdge': '16',
  'elk.spacing.edgeNode': '20',
  'elk.edgeRouting': 'ORTHOGONAL',
  'elk.layered.nodePlacement.strategy': 'BRANDES_KOEPF',
  'elk.layered.nodePlacement.favorStraightEdges': 'true',
  'elk.layered.nodePlacement.bk.fixedAlignment': 'BALANCED',
  'elk.layered.cycleBreaking.strategy': 'DEPTH_FIRST',
  'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
  'elk.layered.unnecessaryBendpoints': 'true',
  'elk.layered.compaction.connectedComponents': 'true',
  'elk.layered.compaction.postCompaction.strategy': 'EDGE_LENGTH',
  'elk.padding': `[top=${NODE_GAP},left=${HORIZONTAL_GAP},bottom=${NODE_GAP * 2},right=${HORIZONTAL_GAP}]`,
  'elk.separateConnectedComponents': 'false',
  'org.eclipse.elk.layered.mergeEdges': 'false',
}

const BRANCH_GAP = NODE_GAP
const CYCLE_LANE_OFFSET = NODE_GAP + 24

type PositionMap = Map<string, { x: number; y: number; width: number; height: number }>

const getNodeSize = (node: Node) => ({
  width: node.width ?? 150,
  height: node.height ?? 50,
})

const getHandlePoint = (
  position: { x: number; y: number; width: number; height: number },
  side: 'NORTH' | 'SOUTH' | 'EAST' | 'WEST',
  offset = 0.5
): Point => {
  switch (side) {
    case 'NORTH':
      return { x: position.x + position.width * offset, y: position.y }
    case 'SOUTH':
      return { x: position.x + position.width * offset, y: position.y + position.height }
    case 'WEST':
      return { x: position.x, y: position.y + position.height * offset }
    case 'EAST':
    default:
      return { x: position.x + position.width, y: position.y + position.height * offset }
  }
}

const routeEdge = (
  edge: Edge,
  nodesById: Map<string, Node>,
  positions: PositionMap,
  channelIndex: number,
  channelCount: number
): { path: string; labelPoint: Point } | undefined => {
  const sourceNode = nodesById.get(edge.source)
  const targetNode = nodesById.get(edge.target)
  const sourcePos = positions.get(edge.source)
  const targetPos = positions.get(edge.target)
  if (!sourceNode || !targetNode || !sourcePos || !targetPos) return undefined

  const sourceHandle = edge.sourceHandle ?? 'source-0'
  const targetHandle = edge.targetHandle ?? 'target-0'
  const source = getHandleSideAndOffset(sourceNode, sourceHandle, 'source')
  const target = getHandleSideAndOffset(targetNode, targetHandle, 'target')
  const sourcePoint = getHandlePoint(sourcePos, source.side, source.offset)
  const targetPoint = getHandlePoint(targetPos, target.side, target.offset)

  if (targetNode.type === 'cycle') {
    const laneY = Math.max(sourcePoint.y, targetPoint.y) + 40
    const points = [sourcePoint, { x: sourcePoint.x, y: laneY }, { x: targetPoint.x, y: laneY }, targetPoint]
    return { path: pointsToPath(points), labelPoint: pathMidpoint(points) }
  }

  if (sourceNode.type === 'cycle') {
    const laneY = sourcePoint.y
    const points = [sourcePoint, { x: targetPoint.x - 40, y: laneY }, { x: targetPoint.x - 40, y: targetPoint.y }, targetPoint]
    return { path: pointsToPath(points), labelPoint: pathMidpoint(points) }
  }

  if (source.side === 'NORTH' || source.side === 'SOUTH') {
    const verticalGap = source.side === 'NORTH' ? -30 : 30
    const bend = { x: sourcePoint.x + (targetPoint.x - sourcePoint.x) * 0.4, y: sourcePoint.y + verticalGap }
    const points = [sourcePoint, { x: sourcePoint.x, y: bend.y }, bend, { x: targetPoint.x, y: bend.y }, targetPoint]
    return { path: pointsToPath(points), labelPoint: pathMidpoint(points) }
  }

  return routeForwardEdge(sourcePoint, targetPoint, channelIndex, channelCount)
}

const buildElkGraph = (nodes: Node[], edges: Edge[]): ElkNode => ({
  id: 'root',
  layoutOptions: LAYOUT_OPTIONS,
  children: nodes.map(node => {
    const size = getNodeSize(node)
    return {
      id: node.id,
      ...size,
    }
  }),
  edges: edges.map(edge => ({
    id: edge.id,
    sources: [edge.source],
    targets: [edge.target],
  })),
})

const collectDescendants = (startId: string, edges: Edge[]): Set<string> => {
  const descendants = new Set<string>()
  const queue = [startId]

  while (queue.length > 0) {
    const current = queue.shift()!
    for (const edge of edges) {
      if (edge.source === current && !descendants.has(edge.target)) {
        descendants.add(edge.target)
        queue.push(edge.target)
      }
    }
  }

  return descendants
}

const shiftSubtree = (
  positions: PositionMap,
  edges: Edge[],
  rootId: string,
  deltaY: number
) => {
  const toShift = new Set([rootId, ...Array.from(collectDescendants(rootId, edges))])
  toShift.forEach(nodeId => {
    const pos = positions.get(nodeId)
    if (pos) positions.set(nodeId, { ...pos, y: pos.y + deltaY })
  })
}

const subtreeBounds = (positions: PositionMap, edges: Edge[], rootId: string) => {
  const ids = new Set([rootId, ...Array.from(collectDescendants(rootId, edges))])
  let minY = Infinity
  let maxY = -Infinity

  ids.forEach(nodeId => {
    const pos = positions.get(nodeId)
    if (!pos) return
    minY = Math.min(minY, pos.y)
    maxY = Math.max(maxY, pos.y + pos.height)
  })

  return { minY, maxY }
}

const refineBranchLayout = (
  nodes: Node[],
  edges: Edge[],
  positions: PositionMap,
  columns: Map<string, number>
) => {
  const nopNodes = nodes.filter(node => node.type === 'nop')

  for (const nop of nopNodes) {
    const outgoing = edges.filter(edge => edge.source === nop.id)
    const branchEdges = outgoing.filter(edge => !edge.target.startsWith('cycle-'))
    if (branchEdges.length < 2) continue

    const nopPos = positions.get(nop.id)
    if (!nopPos) continue

    const branchColumns = branchEdges.map(edge => columns.get(edge.target) ?? 0)
    const usesHorizontalLanes = new Set(branchColumns).size === branchColumns.length

    if (usesHorizontalLanes) {
      const targetY = nopPos.y + nopPos.height / 2
      branchEdges.forEach(edge => {
        const targetPos = positions.get(edge.target)
        if (!targetPos) return
        shiftSubtree(positions, edges, edge.target, targetY - targetPos.height / 2 - targetPos.y)
      })
      continue
    }

    const sorted = [...branchEdges].sort((a, b) => {
      const aY = positions.get(a.target)?.y ?? 0
      const bY = positions.get(b.target)?.y ?? 0
      return aY - bY
    })

    for (let i = 1; i < sorted.length; i++) {
      const prev = sorted[i - 1]
      const current = sorted[i]
      const prevBounds = subtreeBounds(positions, edges, prev.target)
      const currentBounds = subtreeBounds(positions, edges, current.target)
      const overlap = prevBounds.maxY + BRANCH_GAP - currentBounds.minY
      if (overlap > 0) {
        shiftSubtree(positions, edges, current.target, overlap)
      }
    }
  }
}

const refineCycleLayout = (nodes: Node[], edges: Edge[], positions: PositionMap) => {
  const cycleNodes = nodes.filter(node => node.type === 'cycle')

  for (const cycleNode of cycleNodes) {
    const loopTargetId = cycleNode.data?.outgoingEdges?.[0]?.sinkNodeName as string | undefined
    if (!loopTargetId) continue

    const incoming = edges.find(edge => edge.target === cycleNode.id)
    const loopTarget = positions.get(loopTargetId)
    const loopTail = incoming ? positions.get(incoming.source) : undefined
    if (!loopTarget) continue

    let maxY = loopTarget.y + loopTarget.height
    if (loopTail) maxY = Math.max(maxY, loopTail.y + loopTail.height)

    const loopBody = collectDescendants(loopTargetId, edges)
    Array.from(loopBody).forEach(nodeId => {
      const pos = positions.get(nodeId)
      if (pos) maxY = Math.max(maxY, pos.y + pos.height)
    })

    positions.set(cycleNode.id, {
      ...(positions.get(cycleNode.id) ?? getNodeSize(cycleNode)),
      x: loopTarget.x,
      y: maxY + CYCLE_LANE_OFFSET,
      width: positions.get(cycleNode.id)?.width ?? getNodeSize(cycleNode).width,
      height: positions.get(cycleNode.id)?.height ?? getNodeSize(cycleNode).height,
    })
  }
}

const refineMergeLayout = (nodes: Node[], edges: Edge[], positions: PositionMap) => {
  for (const node of nodes) {
    if (node.type !== 'nop') continue
    const incoming = edges.filter(edge => edge.target === node.id)
    if (incoming.length < 2) continue

    const sourcePositions = incoming
      .map(edge => positions.get(edge.source))
      .filter((pos): pos is NonNullable<typeof pos> => pos !== undefined)

    if (sourcePositions.length < 2) continue

    const centerY =
      sourcePositions.reduce((sum, pos) => sum + pos.y + pos.height / 2, 0) / sourcePositions.length
    const current = positions.get(node.id)
    if (!current) continue

    positions.set(node.id, {
      ...current,
      y: centerY - current.height / 2,
    })
  }
}

const assignLane = (
  nodeId: string,
  column: number,
  columns: Map<string, number>,
  forwardEdges: Edge[],
  nodes: Node[]
): void => {
  columns.set(nodeId, Math.max(columns.get(nodeId) ?? 0, column))

  const incomingCount = forwardEdges.filter(edge => edge.target === nodeId).length
  const outgoing = forwardEdges.filter(edge => edge.source === nodeId)
  const node = nodes.find(item => item.id === nodeId)

  if (node?.type === 'nop' && outgoing.length > 1) return
  if (incomingCount > 1) return

  outgoing.forEach(edge => assignLane(edge.target, column + 1, columns, forwardEdges, nodes))
}

const assignNodeColumns = (nodes: Node[], edges: Edge[]): Map<string, number> => {
  const columns = new Map<string, number>()
  const forwardEdges = edges.filter(
    edge => !edge.target.startsWith('cycle-') && nodes.find(node => node.id === edge.source)?.type !== 'cycle'
  )

  const entryIds = nodes.filter(node => node.type === 'entrypoint').map(node => node.id)
  const targets = new Set(forwardEdges.map(edge => edge.target))
  const roots =
    entryIds.length > 0 ? entryIds : nodes.filter(node => !targets.has(node.id)).map(node => node.id)

  roots.forEach(id => columns.set(id, 0))

  for (let pass = 0; pass < nodes.length; pass++) {
    forwardEdges.forEach(edge => {
      const sourceCol = columns.get(edge.source)
      if (sourceCol === undefined) return
      columns.set(edge.target, Math.max(columns.get(edge.target) ?? 0, sourceCol + 1))
    })
  }

  const nopNodes = nodes.filter(node => node.type === 'nop')
  for (const nop of nopNodes) {
    const branchEdges = forwardEdges.filter(edge => edge.source === nop.id)
    if (branchEdges.length < 2) continue

    const sorted = [...branchEdges].sort((a, b) => {
      const aIdx = parseInt(a.sourceHandle?.split('-')[1] ?? '0', 10)
      const bIdx = parseInt(b.sourceHandle?.split('-')[1] ?? '0', 10)
      return aIdx - bIdx
    })

    const baseCol = (columns.get(nop.id) ?? 0) + 1
    sorted.forEach((edge, index) => assignLane(edge.target, baseCol + index, columns, forwardEdges, nodes))
  }

  for (const node of nopNodes) {
    const incoming = forwardEdges.filter(edge => edge.target === node.id)
    if (incoming.length < 2) continue
    const mergeCol = Math.max(...incoming.map(edge => columns.get(edge.source) ?? 0)) + 1
    assignLane(node.id, mergeCol, columns, forwardEdges, nodes)
  }

  nodes.forEach(node => {
    if (!columns.has(node.id)) columns.set(node.id, 0)
  })

  return columns
}

const compactHorizontalLayout = (
  nodes: Node[],
  edges: Edge[],
  positions: PositionMap,
  columns: Map<string, number>
) => {
  const byColumn = new Map<number, string[]>()

  columns.forEach((column, nodeId) => {
    const columnNodes = byColumn.get(column) ?? []
    columnNodes.push(nodeId)
    byColumn.set(column, columnNodes)
  })

  const sortedColumns = Array.from(byColumn.keys()).sort((a, b) => a - b)
  let nextX = HORIZONTAL_GAP

  for (const column of sortedColumns) {
    const nodeIds = byColumn.get(column)!
    const maxWidth = Math.max(...nodeIds.map(nodeId => positions.get(nodeId)?.width ?? 150))
    for (const nodeId of nodeIds) {
      const pos = positions.get(nodeId)
      if (pos) positions.set(nodeId, { ...pos, x: nextX })
    }
    nextX += maxWidth + HORIZONTAL_GAP
  }
}

export type LayoutResult = {
  nodes: Node[]
  edges: Edge[]
}

export const layoutWorkflow = async (nodes: Node[], edges: Edge[]): Promise<LayoutResult> => {
  const elkGraph = buildElkGraph(nodes, edges)
  const laidOutGraph = await elk.layout(elkGraph)

  const positions: PositionMap = new Map()
  nodes.forEach(node => {
    const elkNode = laidOutGraph.children?.find(child => child.id === node.id)
    const size = getNodeSize(node)
    positions.set(node.id, {
      x: elkNode?.x ?? 0,
      y: elkNode?.y ?? 0,
      width: size.width,
      height: size.height,
    })
  })

  const columns = assignNodeColumns(nodes, edges)

  refineBranchLayout(nodes, edges, positions, columns)
  refineMergeLayout(nodes, edges, positions)
  compactHorizontalLayout(nodes, edges, positions, columns)
  refineCycleLayout(nodes, edges, positions)

  const nodesById = new Map(nodes.map(node => [node.id, node]))
  const parallelGroups = new Map<string, Edge[]>()
  edges.forEach(edge => {
    const key = `${edge.source}->${edge.target}`
    const group = parallelGroups.get(key) ?? []
    group.push(edge)
    parallelGroups.set(key, group)
  })

  const laidOutNodes = nodes.map(node => {
    const pos = positions.get(node.id)
    return {
      ...node,
      position: { x: pos?.x ?? 0, y: pos?.y ?? 0 },
      isLaidOut: true,
    }
  })

  const laidOutEdges = edges.map(edge => {
    const group = parallelGroups.get(`${edge.source}->${edge.target}`) ?? [edge]
    const channelIndex = group.findIndex(item => item.id === edge.id)
    const routed = routeEdge(edge, nodesById, positions, channelIndex, group.length)

    return {
      ...edge,
      data: {
        ...edge.data,
        layoutPath: routed?.path,
        layoutLabelPoint: routed?.labelPoint,
      },
    }
  })

  return { nodes: laidOutNodes, edges: laidOutEdges }
}
