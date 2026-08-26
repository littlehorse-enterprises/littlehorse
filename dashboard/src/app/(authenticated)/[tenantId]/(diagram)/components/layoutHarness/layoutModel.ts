/**
 * A headless, deterministic model of the dashboard's diagram pipeline.
 *
 * It runs the app's OWN code wherever that code is pure — getCycleNodes,
 * extractNodes, extractEdges, LayoutManager's exported ELK_LAYOUT_OPTIONS
 * (imported, so the tests can never drift from the app), the ELK layout
 * itself, and the same route helpers Default.tsx renders with. Edge geometry
 * is ELK's routed sections, exactly as the app now draws them; the
 * smooth-step fallback (used by the app only before the first layout pass)
 * is modeled for completeness but should never be hit here.
 *
 * The only modeled quantities are node dimensions (from each component's
 * Tailwind classes) and handle placement for the fallback path.
 */
import ELK, { type ElkNode } from 'elkjs/lib/elk.bundled.js'
import { Position, getSmoothStepPath } from 'reactflow'
import { ThreadSpec } from 'littlehorse-client/proto'
import { ELK_LAYOUT_OPTIONS } from '../LayoutManager'
import { extractEdges } from '../EdgeTypes/extractEdges'
import { routeLabelPoint, routeToPath } from '../EdgeTypes/elkRoute'
import { extractNodes, getCycleNodes } from '../NodeTypes/extractNodes'
import { nopSourceHandlePlacements } from '../NodeTypes/nopHandleLayout'
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

const handleIndexOf = (handleId: string | null | undefined): number => {
  const n = Number(handleId?.split('-').pop())
  return Number.isFinite(n) ? n : 0
}

/**
 * Handle contract + fallback geometry, per the component sources: every type
 * renders source-0 (right) and target-0 (left) — except Cycle (inverted:
 * source left, target right) and Nop (one source per outgoing edge, placed by
 * nopSourceHandlePlacements — the app's own function, imported).
 */
const sourceHandle = (
  node: SceneNode,
  handleIndex: number,
  outgoing: { sinkNodeName: string }[]
): { pt: Pt; side: Position; missing: boolean } => {
  if (node.type === 'cycle') {
    return { pt: alongSide(node.rect, Position.Left, 0.5), side: Position.Left, missing: handleIndex > 0 }
  }
  if (node.type === 'nop') {
    const placements = nopSourceHandlePlacements(outgoing as never)
    const missing = handleIndex >= placements.length
    const placement = placements[Math.min(handleIndex, placements.length - 1)]
    return { pt: alongSide(node.rect, placement.position, placement.pct), side: placement.position, missing }
  }
  return { pt: alongSide(node.rect, Position.Right, 0.5), side: Position.Right, missing: handleIndex > 0 }
}

const targetHandle = (node: SceneNode, handleIndex: number): { pt: Pt; side: Position; missing: boolean } => {
  if (node.type === 'cycle') {
    return { pt: alongSide(node.rect, Position.Right, 0.5), side: Position.Right, missing: handleIndex > 0 }
  }
  return { pt: alongSide(node.rect, Position.Left, 0.5), side: Position.Left, missing: handleIndex > 0 }
}

/**
 * WfSpec ThreadSpec -> laid-out scene, through the app's own pipeline:
 * getCycleNodes -> extractNodes/extractEdges -> ELK with the app's exported
 * options -> node positions AND routed edge sections, exactly like
 * LayoutManager -> label points via the app's routeLabelPoint.
 */
export const buildScene = async (threadSpec: ThreadSpec): Promise<Scene> => {
  // getCycleNodes mutates; feed it a deep copy so fixtures stay pristine.
  // (ThreadSpec.clone is protobuf-ts's own deep copy — jsdom lacks structuredClone.)
  const spec: ThreadSpec = ThreadSpec.clone(threadSpec)
  getCycleNodes(spec)
  const rfNodes = extractNodes(spec)
  const rfEdges = extractEdges(spec)

  const elkGraph: ElkNode = {
    id: 'root',
    layoutOptions: ELK_LAYOUT_OPTIONS,
    children: rfNodes.map(node => {
      const { w, h } = dimsFor(node.type ?? 'task')
      return { id: node.id, width: w, height: h }
    }),
    edges: rfEdges.map(edge => ({ id: edge.id, sources: [edge.source], targets: [edge.target] })),
  }
  const laidOut = await elk.layout(elkGraph)
  const positioned = new Map((laidOut.children ?? []).map(child => [child.id, child]))
  const routeById = new Map(
    (laidOut.edges ?? []).flatMap(edge => {
      const section = edge.sections?.[0]
      if (!section) return []
      const points = [section.startPoint, ...(section.bendPoints ?? []), section.endPoint]
      return [[edge.id, points.map(p => ({ x: p.x, y: p.y }))] as const]
    })
  )

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

    // The app's rendering rule, mirrored: ELK route when present, else the
    // smooth-step fallback from handle geometry.
    const route = routeById.get(edge.id)
    let polyline: Polyline
    let labelPt: Pt
    if (route !== undefined && route.length >= 2) {
      polyline = pathToPolyline(routeToPath(route))
      labelPt = routeLabelPoint(route)
    } else {
      const [path, labelX, labelY] = getSmoothStepPath({
        sourceX: src.pt.x,
        sourceY: src.pt.y,
        sourcePosition: src.side,
        targetX: tgt.pt.x,
        targetY: tgt.pt.y,
        targetPosition: tgt.side,
        borderRadius: 0,
      })
      polyline = pathToPolyline(path)
      labelPt = { x: labelX, y: labelY }
    }

    const data = edge.data as {
      edgeCondition?: { oneofKind?: string }
      isElseEdge?: boolean
      variableMutations?: unknown[]
    }
    const hasLabel =
      data?.edgeCondition?.oneofKind !== undefined ||
      data?.isElseEdge === true ||
      (data?.variableMutations?.length ?? 0) > 0
    // Condition/else chip footprint (scaled 0.75 in Default.tsx), centered on the label point.
    const labelRect: Rect | undefined = hasLabel ? { x: labelPt.x - 23, y: labelPt.y - 10, w: 46, h: 20 } : undefined

    return {
      id: edge.id,
      source: edge.source,
      target: edge.target,
      isCycleEdge: edge.source.startsWith('cycle-') || edge.target.startsWith('cycle-'),
      hasLabel,
      labelRect,
      polyline,
      missingSourceHandle: src.missing,
      missingTargetHandle: tgt.missing,
    }
  })

  return { nodes: sceneNodes, edges: sceneEdges }
}
