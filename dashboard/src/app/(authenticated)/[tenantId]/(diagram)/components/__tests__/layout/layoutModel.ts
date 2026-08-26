/**
 * A headless, deterministic model of the dashboard's diagram pipeline.
 *
 * It runs the app's OWN code wherever that code is pure — getCycleNodes,
 * extractNodes, extractEdges, the exact ELK options from LayoutManager, and
 * reactflow's getSmoothStepPath (the same function Default.tsx renders with,
 * borderRadius 0). The only modeled parts are what the browser measures at
 * runtime: node dimensions (from each component's Tailwind classes) and
 * handle placement (replicated from each component's Handle declarations,
 * including Nop's distribution logic and Cycle's inverted handles).
 *
 * Because the model is declared and versioned here, every geometric claim in
 * the invariants is reproducible: same fixture in, same coordinates out.
 */
import ELK from 'elkjs/lib/elk.bundled.js'
import { Position, getSmoothStepPath } from 'reactflow'
import { ThreadSpec } from 'littlehorse-client/proto'
import { extractEdges } from '../../EdgeTypes/extractEdges'
import { extractNodes, getCycleNodes } from '../../NodeTypes/extractNodes'
import { Polyline, Pt, Rect, pathToPolyline } from './geometry'

const elk = new ELK()

/**
 * Rendered node dimensions by type, from the component sources:
 * h-6/w-6 = 24, h-8/w-8 = 32, h-10/w-10 = 40; boxed nodes (task, userTask,
 * externalEvent, ...) use LayoutManager's fallback footprint of 150x50 —
 * the same numbers ELK is given for them, so model and layout agree.
 */
const DIMS: Record<string, { w: number; h: number }> = {
  entrypoint: { w: 24, h: 24 },
  exit: { w: 24, h: 24 },
  nop: { w: 32, h: 32 },
  cycle: { w: 40, h: 40 },
  sleep: { w: 40, h: 40 },
  startThread: { w: 40, h: 40 },
  waitForThreads: { w: 40, h: 40 },
  runChildWf: { w: 40, h: 40 },
  waitForChildWf: { w: 40, h: 40 },
  startMultipleThreads: { w: 40, h: 40 },
  task: { w: 150, h: 50 },
  userTask: { w: 150, h: 50 },
  externalEvent: { w: 150, h: 50 },
  throwEvent: { w: 150, h: 50 },
  waitForCondition: { w: 150, h: 50 },
}
const dimsFor = (type: string) => DIMS[type] ?? { w: 150, h: 50 }

export interface SceneNode {
  id: string
  type: string
  rect: Rect
}

export interface SceneEdge {
  id: string
  source: string
  target: string
  isCycleEdge: boolean
  hasLabel: boolean
  labelRect?: Rect
  polyline: Polyline
  /** extractEdges asked for a handle index the component does not render. */
  missingSourceHandle: boolean
  missingTargetHandle: boolean
}

export interface Scene {
  nodes: SceneNode[]
  edges: SceneEdge[]
}

interface HandlePoint {
  pt: Pt
  side: Position
}

/** Percentage-positioned point along one side of a rect (reactflow handle style). */
const alongSide = (rect: Rect, side: Position, pct: number): Pt => {
  switch (side) {
    case Position.Top:
      return { x: rect.x + rect.w * pct, y: rect.y }
    case Position.Bottom:
      return { x: rect.x + rect.w * pct, y: rect.y + rect.h }
    case Position.Left:
      return { x: rect.x, y: rect.y + rect.h * pct }
    case Position.Right:
      return { x: rect.x + rect.w, y: rect.y + rect.h * pct }
  }
}

interface NopHandle {
  side: Position
  pct: number
}

/**
 * Replicates Nop.tsx#generateSourceHandles exactly: the same branch structure,
 * the same distributeHandles percentages, keyed by the node's outgoing edges.
 */
const nopSourceHandles = (outgoing: { sinkNodeName: string }[]): NopHandle[] => {
  const cycleEdges = outgoing.filter(e => e.sinkNodeName.startsWith('cycle-'))
  const regularCount = outgoing.length - cycleEdges.length
  const totalCount = outgoing.length
  const hasCycle = cycleEdges.length > 0
  const distribute = (count: number, side: Position): NopHandle[] =>
    Array.from({ length: count }, (_, i) => ({ side, pct: (i + 1) / (count + 1) }))

  if (hasCycle) {
    if (regularCount === 1) return [{ side: Position.Right, pct: 0.5 }, { side: Position.Bottom, pct: 0.5 }]
    if (regularCount === 2)
      return [
        { side: Position.Top, pct: 0.33 },
        { side: Position.Top, pct: 0.66 },
        { side: Position.Bottom, pct: 0.5 },
      ]
    const half = Math.floor(regularCount / 2)
    if (regularCount % 2 !== 0)
      return [...distribute(half, Position.Top), { side: Position.Right, pct: 0.5 }, { side: Position.Bottom, pct: 0.5 }]
    return [...distribute(half, Position.Top), ...distribute(half, Position.Right), { side: Position.Bottom, pct: 0.5 }]
  }
  if (totalCount === 1) return [{ side: Position.Right, pct: 0.5 }]
  if (totalCount === 2) return [{ side: Position.Top, pct: 0.5 }, { side: Position.Bottom, pct: 0.5 }]
  if (totalCount === 3)
    return [
      { side: Position.Top, pct: 0.5 },
      { side: Position.Right, pct: 0.5 },
      { side: Position.Bottom, pct: 0.5 },
    ]
  const half = Math.floor(totalCount / 2)
  if (totalCount % 2 !== 0)
    return [...distribute(half, Position.Top), { side: Position.Right, pct: 0.5 }, ...distribute(half, Position.Bottom)]
  return [...distribute(half, Position.Top), ...distribute(half, Position.Bottom)]
}

/** Source-handle geometry per component (see the component files). */
const sourceHandle = (
  node: SceneNode,
  handleIndex: number,
  outgoing: { sinkNodeName: string }[]
): { handle: HandlePoint; missing: boolean } => {
  if (node.type === 'cycle') {
    // Cycle.tsx: source faces LEFT.
    return { handle: { pt: alongSide(node.rect, Position.Left, 0.5), side: Position.Left }, missing: false }
  }
  if (node.type === 'nop') {
    const handles = nopSourceHandles(outgoing)
    const missing = handleIndex >= handles.length
    const h = handles[Math.min(handleIndex, handles.length - 1)]
    return { handle: { pt: alongSide(node.rect, h.side, h.pct), side: h.side }, missing }
  }
  // Every other component renders exactly one source handle on the right.
  return {
    handle: { pt: alongSide(node.rect, Position.Right, 0.5), side: Position.Right },
    missing: handleIndex > 0,
  }
}

const targetHandle = (node: SceneNode, handleIndex: number): { handle: HandlePoint; missing: boolean } => {
  if (node.type === 'cycle') {
    // Cycle.tsx: target faces RIGHT.
    return { handle: { pt: alongSide(node.rect, Position.Right, 0.5), side: Position.Right }, missing: false }
  }
  // Every component (including Nop) renders exactly one target handle on the left.
  return {
    handle: { pt: alongSide(node.rect, Position.Left, 0.5), side: Position.Left },
    missing: handleIndex > 0,
  }
}

const handleIndexOf = (handleId: string | null | undefined): number => {
  const n = Number(handleId?.split('-').pop())
  return Number.isFinite(n) ? n : 0
}

/** The exact options LayoutManager.tsx passes to ELK. */
const ELK_OPTIONS = {
  'elk.algorithm': 'layered',
  'elk.direction': 'RIGHT',
  'elk.spacing.nodeNode': '150',
  'elk.layered.spacing.nodeNodeBetweenLayers': '200',
  'elk.spacing.edgeEdge': '100',
  'elk.spacing.edgeNode': '100',
  'elk.edgeRouting': 'ORTHOGONAL',
  'elk.layered.nodePlacement.strategy': 'LINEAR_SEGMENTS',
  'elk.layered.cycleBreaking.strategy': 'DEPTH_FIRST',
  'elk.layered.considerModelOrder.strategy': 'NODES_AND_EDGES',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
  'elk.layered.unnecessaryBendpoints': 'true',
  'elk.layered.compaction.postCompaction.strategy': 'EDGE_LENGTH',
  'elk.padding': '[top=100,left=100,bottom=100,right=100]',
  'elk.separateConnectedComponents': 'false',
  'org.eclipse.elk.layered.mergeEdges': 'false',
}

/**
 * WfSpec ThreadSpec -> laid-out scene, through the app's own pipeline:
 * getCycleNodes -> extractNodes/extractEdges -> ELK (positions only, exactly
 * like LayoutManager, including its cycle-x and exit-y post-adjustments) ->
 * per-edge smooth-step paths from the modeled handle geometry.
 */
export const buildScene = async (threadSpec: ThreadSpec): Promise<Scene> => {
  // getCycleNodes mutates; feed it a deep copy so fixtures stay pristine.
  // (ThreadSpec.clone is protobuf-ts's own deep copy — jsdom lacks structuredClone.)
  const spec: ThreadSpec = ThreadSpec.clone(threadSpec)
  getCycleNodes(spec)
  const rfNodes = extractNodes(spec)
  const rfEdges = extractEdges(spec)

  const elkGraph = {
    id: 'root',
    layoutOptions: ELK_OPTIONS,
    children: rfNodes.map(node => {
      const { w, h } = dimsFor(node.type ?? 'task')
      return { id: node.id, width: w, height: h }
    }),
    edges: rfEdges.map(edge => ({ id: `${edge.source}-${edge.target}`, sources: [edge.source], targets: [edge.target] })),
  }
  const laidOut = await elk.layout(elkGraph)
  const positioned = new Map((laidOut.children ?? []).map(child => [child.id, child]))

  // LayoutManager's post-adjustments, replicated verbatim.
  const hasCycles = rfNodes.some(node => node.type === 'cycle')
  for (const node of rfNodes) {
    const elkNode = positioned.get(node.id)
    if (!elkNode) continue
    if (node.type === 'cycle' && elkNode.x !== undefined) {
      const initialNode = positioned.get((node.data as { outgoingEdges: { sinkNodeName: string }[] }).outgoingEdges[0].sinkNodeName)
      const cycleNodeX = elkNode.x - (initialNode?.x ?? 0)
      elkNode.x = ((initialNode?.x ?? 0) + cycleNodeX) / 2
    }
    if (node.type === 'exit' && hasCycles) {
      const initialNode = (laidOut.children ?? []).find(n => n.id.includes('ENTRYPOINT'))
      if (initialNode?.y !== undefined && elkNode.y !== initialNode.y) {
        elkNode.y = initialNode.y
      }
    }
  }

  const sceneNodes: SceneNode[] = rfNodes.map(node => {
    const elkNode = positioned.get(node.id)
    const { w, h } = dimsFor(node.type ?? 'task')
    return { id: node.id, type: node.type ?? 'task', rect: { x: elkNode?.x ?? 0, y: elkNode?.y ?? 0, w, h } }
  })
  const byId = new Map(sceneNodes.map(node => [node.id, node]))
  const outgoingByNode = new Map(Object.entries(spec.nodes).map(([id, node]) => [id, node.outgoingEdges]))

  const sceneEdges: SceneEdge[] = rfEdges.map(edge => {
    const sourceNode = byId.get(edge.source)!
    const targetNode = byId.get(edge.target)!
    const src = sourceHandle(sourceNode, handleIndexOf(edge.sourceHandle), outgoingByNode.get(edge.source) ?? [])
    const tgt = targetHandle(targetNode, handleIndexOf(edge.targetHandle))

    // Default.tsx: getSmoothStepPath with borderRadius 0.
    const [path, labelX, labelY] = getSmoothStepPath({
      sourceX: src.handle.pt.x,
      sourceY: src.handle.pt.y,
      sourcePosition: src.handle.side,
      targetX: tgt.handle.pt.x,
      targetY: tgt.handle.pt.y,
      targetPosition: tgt.handle.side,
      borderRadius: 0,
    })

    const data = edge.data as { edgeCondition?: { oneofKind?: string }; isElseEdge?: boolean; variableMutations?: unknown[] }
    const hasLabel =
      data?.edgeCondition?.oneofKind !== undefined || data?.isElseEdge === true || (data?.variableMutations?.length ?? 0) > 0
    // Condition/else chip footprint (scaled 0.75 in Default.tsx), centered on the label point.
    const labelRect: Rect | undefined = hasLabel ? { x: labelX - 23, y: labelY - 10, w: 46, h: 20 } : undefined

    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      isCycleEdge: edge.source.startsWith('cycle-') || edge.target.startsWith('cycle-'),
      hasLabel,
      labelRect,
      polyline: pathToPolyline(path),
      missingSourceHandle: src.missing,
      missingTargetHandle: tgt.missing,
    }
  })

  return { nodes: sceneNodes, edges: sceneEdges }
}
