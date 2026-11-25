'use client'
import { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import { rescueWfRun } from '@/app/actions/rescueWfRun'
import { resumeWfRun } from '@/app/actions/resumeWfRun'
import { stopWfRun } from '@/app/actions/stopWfRun'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { WorkflowDefinition } from '@/types'
import { LHStatus, ThreadSpec, WfRun } from 'littlehorse-client/proto'
import { EyeIcon, EyeOffIcon, PlayCircleIcon, RotateCcwIcon, StopCircleIcon } from 'lucide-react'
import { usePathname, useSearchParams } from 'next/navigation'
import { FC, ReactNode, useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import ReactFlow, {
  Controls,
  Edge as RFEdge,
  Panel,
  Node as RFNode,
  useEdgesState,
  useNodesState,
  type OnSelectionChangeParams,
  type Viewport,
} from 'reactflow'
import 'reactflow/dist/base.css'
import { DiagramProvider, NodeInContext, ThreadType } from '../context'
import edgeTypes from './EdgeTypes'
import { isBranchEdgeReached } from './EdgeTypes/edgeConditionDisplay'
import { extractEdges } from './EdgeTypes/extractEdges'
import {
  extractExceptionHandlers,
  findHandlerRuns,
  listHandlerGroups,
  rootSourceNodeId,
  type HandlerView,
} from './ExceptionHandlers/exceptionHandlers'
import { getNodeRunsList, LayoutManager, NodeRunsResolver } from './LayoutManager'
import nodeTypes from './NodeTypes'
import { extractNodes, getCycleNodes, getNodeAfterEntrypoint } from './NodeTypes/extractNodes'
import { Sidebar } from './Sidebar'
import { ThreadPanel } from './ThreadPanel'

type Props = {
  wfRun?: Omit<WfRun, 'threadRuns'> & { threadRuns: ThreadRunWithNodeRuns[] }
  spec: WorkflowDefinition
  definitionId: string
  onThreadChange?: (thread: ThreadType) => void
  headerActions?: ReactNode
}
const threadFromUrl = (
  wfRun: Props['wfRun'],
  spec: WorkflowDefinition,
  threadRunNumber: number | null,
  threadName: string | null
): ThreadType => {
  if (!wfRun) {
    const name = threadName && spec.threadSpecs[threadName] ? threadName : spec.entrypointThreadName
    return { name, number: 0 }
  }
  if (threadRunNumber !== null) {
    const tr = wfRun.threadRuns.find(t => t.number === threadRunNumber)
    if (tr) return { name: tr.threadSpecName, number: tr.number }
  }
  const entrypoint = wfRun.threadRuns.find(tr => tr.number === 0)
  return {
    name: entrypoint?.threadSpecName ?? spec.entrypointThreadName,
    number: entrypoint?.number ?? 0,
  }
}

const extractDiagram = (spec: WorkflowDefinition, threadName: string, threadSpec: ThreadSpec, view?: HandlerView) => {
  const handlers = extractExceptionHandlers(spec, threadName, view)
  if (handlers.nodes.length === 0) return { nodes: extractNodes(threadSpec), edges: extractEdges(threadSpec) }
  // Handler groups paint at z 0, beneath the edges and nodes they contain.
  return {
    nodes: [...extractNodes(threadSpec), ...handlers.nodes].map(n =>
      n.type === 'exceptionHandler' ? n : { ...n, zIndex: 1 }
    ),
    edges: [...extractEdges(threadSpec), ...handlers.edges].map(e => ({ ...e, zIndex: 1 })),
  }
}

/**
 * Per-graph handler state. `key` is the graph it belongs to, so switching
 * spec/run/thread starts from the defaults without an extra render.
 */
type HandlerState = {
  key: string
  /** Handler groups the user has opened on this graph. */
  expanded: string[]
  /** WfSpec page: the top-level node whose handlers are shown because the user selected it. */
  inspected?: string
}

export const Diagram: FC<Props> = ({ spec, definitionId, wfRun, onThreadChange, headerActions }) => {
  const { tenantId } = useWhoAmI()
  const pathname = usePathname()
  const searchParams = useSearchParams()
  const urlThreadRunNumber = searchParams.get('threadRunNumber')
  const threadRunNumber = urlThreadRunNumber ? parseInt(urlThreadRunNumber, 10) : null
  const threadName = searchParams.get('thread')

  const resolvedThread = useMemo(
    () => threadFromUrl(wfRun, spec, Number.isNaN(threadRunNumber) ? null : threadRunNumber, threadName),
    [wfRun, spec, threadRunNumber, threadName]
  )

  const [thread, setThread] = useState<ThreadType>(resolvedThread)

  useEffect(() => {
    setThread(resolvedThread)
  }, [resolvedThread])

  const [node, setNode] = useState<NodeInContext>(undefined)

  const threadSpec = useMemo(() => {
    if (thread === undefined) return spec.threadSpecs[spec.entrypointThreadName]
    const threadSpec = spec.threadSpecs[thread.name]
    getCycleNodes(threadSpec)
    return threadSpec
  }, [spec, thread.name])

  const [edges, setEdges, onEdgesChange] = useEdgesState<RFEdge['data']>([])
  const [nodes, setNodes, onNodesChange] = useNodesState([])

  const threadRun = useMemo(() => wfRun?.threadRuns.find(tr => tr.number === thread.number), [thread.number, wfRun])
  const threadNodeRuns = threadRun?.nodeRuns
  const nodeOutputValues = threadRun?.nodeOutputValues

  const handlerGroups = useMemo(() => listHandlerGroups(spec, thread.name), [spec, thread.name])

  const handlerRuns = useMemo(() => {
    if (!wfRun) return new Map<string, never>()
    return findHandlerRuns(handlerGroups, threadNodeRuns, wfRun.threadRuns)
  }, [handlerGroups, threadNodeRuns, wfRun])

  const resetKey = `${definitionId}|${wfRun?.id ? JSON.stringify(wfRun.id) : ''}|${thread.name}`

  // WfSpec page: handlers stay hidden until the user asks for all of them or
  // selects a node that has some. WfRun page: only handlers that actually ran.
  const [showAllHandlers, setShowAllHandlers] = useState(false)
  const [handlerState, setHandlerState] = useState<HandlerState>({ key: resetKey, expanded: [] })
  const { expanded, inspected } =
    handlerState.key === resetKey ? handlerState : { expanded: [] as string[], inspected: undefined }

  const updateHandlerState = useCallback(
    (update: (current: Omit<HandlerState, 'key'>) => Omit<HandlerState, 'key'>) =>
      setHandlerState(current => ({
        key: resetKey,
        ...update(current.key === resetKey ? current : { expanded: [], inspected: undefined }),
      })),
    [resetKey]
  )

  const toggleHandler = useCallback(
    (groupId: string) =>
      updateHandlerState(current => ({
        ...current,
        expanded: current.expanded.includes(groupId)
          ? current.expanded.filter(id => id !== groupId)
          : [...current.expanded, groupId],
      })),
    [updateHandlerState]
  )

  const visibleGroups = useMemo(
    () =>
      handlerGroups
        .map(group => group.groupId)
        .filter(groupId =>
          wfRun ? handlerRuns.has(groupId) : showAllHandlers || rootSourceNodeId(groupId) === inspected
        ),
    [handlerGroups, handlerRuns, wfRun, showAllHandlers, inspected]
  )

  const handlerView = useMemo<HandlerView>(
    () => ({
      isVisible: groupId => visibleGroups.includes(groupId),
      isExpanded: groupId => expanded.includes(groupId),
      runOf: groupId => handlerRuns.get(groupId)?.run,
    }),
    [visibleGroups, expanded, handlerRuns]
  )

  // Showing, hiding, collapsing or expanding a handler changes the graph, so
  // it gets a layout of its own.
  const layoutKey = `${resetKey}|${visibleGroups.join(',')}|${expanded.filter(id => visibleGroups.includes(id)).join(',')}`

  // Nodes whose handlers are hidden on the WfSpec page get a marker inviting a closer look.
  const hiddenHandlerSources = useMemo(
    () =>
      new Set(
        wfRun
          ? []
          : handlerGroups
              .filter(group => !group.parentGroupId && !visibleGroups.includes(group.groupId))
              .map(group => group.sourceNodeId)
      ),
    [wfRun, handlerGroups, visibleGroups]
  )

  const getNodeRuns = useCallback<NodeRunsResolver>(
    n => {
      const groupId = n.data?.groupId
      if (!wfRun || !groupId) return getNodeRunsList(n.id, threadNodeRuns)
      return getNodeRunsList(n.data.specNodeId, handlerRuns.get(groupId)?.nodeRuns ?? [])
    },
    [wfRun, threadNodeRuns, handlerRuns]
  )

  const lastAppliedThread = useRef<string | null>(null)

  const applyDefaultSelection = useCallback(
    (laidOutNodes: RFNode[]) => {
      if (wfRun) return
      // A re-layout for handlers keeps whatever the user has selected.
      if (lastAppliedThread.current === thread.name) return

      const defaultNodeId = getNodeAfterEntrypoint(threadSpec)
      if (!defaultNodeId) {
        setNode(undefined)
        return
      }

      const target = laidOutNodes.find(n => n.id === defaultNodeId)
      if (!target) return

      lastAppliedThread.current = thread.name
      setNodes(laidOutNodes.map(n => ({ ...n, selected: n.id === defaultNodeId })))
      setNode(target as NodeInContext)
    },
    [wfRun, threadSpec, thread.name, setNodes]
  )

  const appliedResetKey = useRef<string | null>(null)
  const appliedLayoutKey = useRef<string | null>(null)

  const decorateNode = useCallback(
    <T extends RFNode>(n: T): T => {
      const handlersHidden = hiddenHandlerSources.has(n.id) || undefined
      if (!wfRun) return n.data?.handlersHidden === handlersHidden ? n : { ...n, data: { ...n.data, handlersHidden } }
      if (n.type === 'exceptionHandler' || n.type === 'exceptionHandlerChip') {
        const run = handlerRuns.get(n.id)?.run
        return { ...n, data: { ...n.data, run, fade: !run } }
      }
      const nodeRunsList = getNodeRuns(n)
      const fade = nodeRunsList !== undefined && nodeRunsList.length === 0
      return { ...n, data: { ...n.data, fade, nodeRunsList } }
    },
    [hiddenHandlerSources, wfRun, handlerRuns, getNodeRuns]
  )

  const decorateEdge = useCallback(
    (edge: RFEdge): RFEdge => {
      if (!wfRun || !edge.data) return edge
      return {
        ...edge,
        data: {
          ...edge.data,
          nodeOutputValues,
          fade: edge.data.isConditionalBranchEdge
            ? !isBranchEdgeReached(edge.target, threadNodeRuns)
            : edge.data.isExceptionEdge
              ? !handlerRuns.has(edge.data.handlerGroupId!)
              : edge.data.isResumeEdge
                ? handlerRuns.get(edge.data.handlerGroupId!)?.run.status !== LHStatus.COMPLETED
                : undefined,
        },
      }
    },
    [wfRun, nodeOutputValues, threadNodeRuns, handlerRuns]
  )

  useLayoutEffect(() => {
    if (appliedLayoutKey.current === layoutKey) return
    appliedLayoutKey.current = layoutKey
    const extracted = extractDiagram(spec, thread.name, threadSpec, handlerView)
    const extractedNodes = extracted.nodes.map(node => decorateNode({ ...node, data: { ...node.data, layoutKey } }))
    const extractedEdges = extracted.edges.map(decorateEdge)

    // Same graph, handlers shown/hidden/toggled: keep the selection and let
    // LayoutManager re-lay it out in place.
    if (appliedResetKey.current === resetKey) {
      setNodes(current => {
        const selected = new Set(current.filter(n => n.selected).map(n => n.id))
        return extractedNodes.map(n => (selected.has(n.id) ? { ...n, selected: true } : n))
      })
      setEdges(extractedEdges)
      return
    }
    appliedResetKey.current = resetKey
    lastAppliedThread.current = null

    if (!wfRun) {
      const defaultNodeId = getNodeAfterEntrypoint(threadSpec)
      if (defaultNodeId) {
        const target = extractedNodes.find(n => n.id === defaultNodeId)
        if (target) {
          lastAppliedThread.current = thread.name
          setNode(target as NodeInContext)
          setNodes(extractedNodes.map(n => ({ ...n, selected: n.id === defaultNodeId })))
          setEdges(extractedEdges)
          return
        }
      }
    }

    setNode(undefined)
    setNodes(extractedNodes)
    setEdges(extractedEdges)
  }, [
    layoutKey,
    resetKey,
    spec,
    thread.name,
    threadSpec,
    handlerView,
    decorateNode,
    decorateEdge,
    setNodes,
    setEdges,
    wfRun,
  ])

  useEffect(() => {
    setNodes(current => current.map(decorateNode))
    if (wfRun) setNode(current => (current ? decorateNode(current) : current))
  }, [decorateNode, wfRun, setNodes])

  useEffect(() => {
    if (!wfRun) return
    setEdges(current => current.map(decorateEdge))
  }, [decorateEdge, wfRun, setEdges])

  useEffect(() => {
    onThreadChange?.(thread)
  }, [thread, onThreadChange])

  const viewportKey = `lh-viewport:${pathname}:${thread.name}`

  const onMoveEnd = useCallback(
    (_event: unknown, viewport: Viewport) => {
      sessionStorage.setItem(viewportKey, JSON.stringify(viewport))
    },
    [viewportKey]
  )

  const onSelectionChange = useCallback(({ nodes: selectedNodes }: OnSelectionChangeParams) => {
    if (selectedNodes.length === 0) setNode(undefined)
  }, [])

  // WfSpec page: selecting a node shows its handlers (and hides the last
  // node's). Clicks inside a handler box keep it open.
  const onNodeClick = useCallback(
    (_event: unknown, clicked: RFNode) => {
      if (wfRun || clicked.data?.groupId || clicked.type?.startsWith('exceptionHandler')) return
      const hasHandlers = handlerGroups.some(group => !group.parentGroupId && group.sourceNodeId === clicked.id)
      const next = hasHandlers ? clicked.id : undefined
      if (next !== inspected) updateHandlerState(current => ({ ...current, inspected: next }))
    },
    [wfRun, handlerGroups, inspected, updateHandlerState]
  )

  const verb =
    wfRun?.status === LHStatus.RUNNING
      ? 'Stop'
      : wfRun?.status === LHStatus.HALTED
        ? 'Resume'
        : wfRun?.status === LHStatus.ERROR
          ? 'Rescue'
          : ''

  const handleConfirmWfRunAction = useCallback(async () => {
    if (!wfRun) return
    if (wfRun.status === LHStatus.RUNNING) {
      await stopWfRun(tenantId, wfRun.id!)
    } else if (wfRun.status === LHStatus.HALTED) {
      await resumeWfRun(tenantId, wfRun.id!)
    } else if (wfRun.status === LHStatus.ERROR) {
      await rescueWfRun(tenantId, wfRun.id!)
    }
  }, [tenantId, wfRun])

  return (
    <DiagramProvider value={{ thread, setThread, selectedNode: node, setSelectedNode: setNode, wfRun, toggleHandler }}>
      <div className="mb-2 flex items-center justify-between gap-3">
        <div className="min-w-0 flex-1">
          <ThreadPanel spec={spec} wfRun={wfRun} />
        </div>
        {(headerActions || wfRun) && (
          <div className="flex shrink-0 items-center gap-2">
            {headerActions}
            {wfRun && (
              <AlertDialog>
                {wfRun.status === LHStatus.RUNNING && (
                  <AlertDialogTrigger asChild>
                    <Button variant="destructive" className="flex items-center gap-2 font-bold hover:bg-destructive/90">
                      STOP <StopCircleIcon />
                    </Button>
                  </AlertDialogTrigger>
                )}
                {wfRun.status === LHStatus.HALTED && (
                  <AlertDialogTrigger asChild>
                    <Button className="flex items-center gap-2 bg-green-500 font-bold hover:bg-green-600">
                      RESUME <PlayCircleIcon />
                    </Button>
                  </AlertDialogTrigger>
                )}
                {wfRun.status === LHStatus.ERROR && (
                  <AlertDialogTrigger asChild>
                    <Button className="flex items-center gap-2 bg-yellow-500 font-bold hover:bg-yellow-600">
                      RESCUE <RotateCcwIcon />
                    </Button>
                  </AlertDialogTrigger>
                )}
                <AlertDialogContent>
                  <AlertDialogHeader>
                    <AlertDialogTitle>{`Confirm ${verb}`}</AlertDialogTitle>
                    <AlertDialogDescription>
                      {`Are you sure you want to ${verb.toLowerCase()} this workflow run?`}
                    </AlertDialogDescription>
                  </AlertDialogHeader>
                  <AlertDialogFooter>
                    <AlertDialogCancel>Cancel</AlertDialogCancel>
                    <AlertDialogAction onClick={handleConfirmWfRunAction}>Confirm</AlertDialogAction>
                  </AlertDialogFooter>
                </AlertDialogContent>
              </AlertDialog>
            )}
          </div>
        )}
      </div>
      <div className="grid min-h-[600px] grid-cols-[1fr_330px]">
        <div className="mb-4 flex-1 rounded border-2 border-slate-100 bg-slate-50 shadow-inner">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            nodesConnectable={false}
            nodesDraggable={false}
            elementsSelectable
            onlyRenderVisibleElements={true}
            minZoom={0.01}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            snapToGrid={true}
            onMoveEnd={onMoveEnd}
            onSelectionChange={onSelectionChange}
            onNodeClick={onNodeClick}
          >
            <Controls />
            {!wfRun && handlerGroups.length > 0 && (
              <Panel position="top-right">
                <Button
                  variant="outline"
                  size="sm"
                  className="h-7 gap-1.5 bg-white px-2 text-xs text-slate-700 shadow-sm"
                  onClick={() => setShowAllHandlers(show => !show)}
                  aria-pressed={showAllHandlers}
                >
                  {showAllHandlers ? <EyeOffIcon /> : <EyeIcon />}
                  {showAllHandlers ? 'Hide exception handlers' : 'Show exception handlers'}
                </Button>
              </Panel>
            )}
          </ReactFlow>
          <LayoutManager
            layoutKey={layoutKey}
            graphKey={resetKey}
            getNodeRuns={getNodeRuns}
            viewportKey={viewportKey}
            setNodes={setNodes}
            setEdges={setEdges}
            onLayoutComplete={applyDefaultSelection}
          />
        </div>
        <Sidebar showNodeRun={wfRun !== undefined} />
      </div>
    </DiagramProvider>
  )
}
