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
import { LHStatus, WfRun, WfSpec } from 'littlehorse-client/proto'
import { PlayCircleIcon, RotateCcwIcon, StopCircleIcon } from 'lucide-react'
import { usePathname, useSearchParams } from 'next/navigation'
import { FC, ReactNode, useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import ReactFlow, { Controls, Node as RFNode, useEdgesState, useNodesState, type Viewport } from 'reactflow'
import 'reactflow/dist/base.css'
import { DiagramProvider, NodeInContext, ThreadType } from '../context'
import edgeTypes from './EdgeTypes'
import { extractEdges } from './EdgeTypes/extractEdges'
import { LayoutManager } from './LayoutManager'
import nodeTypes from './NodeTypes'
import { extractNodes, getCycleNodes, getNodeAfterEntrypoint } from './NodeTypes/extractNodes'
import { Sidebar } from './Sidebar'
import { ThreadPanel } from './ThreadPanel'

type Props = {
  wfRun?: Omit<WfRun, 'threadRuns'> & { threadRuns: ThreadRunWithNodeRuns[] }
  spec: WfSpec
  onThreadChange?: (thread: ThreadType) => void
  headerActions?: ReactNode
}
const threadFromUrl = (
  wfRun: Props['wfRun'],
  spec: WfSpec,
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
  const greatest = wfRun.threadRuns.find(tr => tr.number === wfRun.greatestThreadrunNumber)
  return {
    name: greatest?.threadSpecName ?? spec.entrypointThreadName,
    number: wfRun.greatestThreadrunNumber ?? 0,
  }
}

export const Diagram: FC<Props> = ({ spec, wfRun, onThreadChange, headerActions }) => {
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

  const [edges, setEdges, onEdgesChange] = useEdgesState(extractEdges(threadSpec))
  const [nodes, setNodes, onNodesChange] = useNodesState(extractNodes(threadSpec))

  const threadNodeRuns = useMemo(() => {
    if (!wfRun) return
    return wfRun.threadRuns.find(tr => tr.number === thread.number)?.nodeRuns
  }, [thread.number, wfRun])

  const lastAppliedThread = useRef<string | null>(null)

  const applyDefaultSelection = useCallback(
    (laidOutNodes: RFNode[]) => {
      if (wfRun) return

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

  useLayoutEffect(() => {
    lastAppliedThread.current = null
    const extractedNodes = extractNodes(threadSpec)
    const extractedEdges = extractEdges(threadSpec)

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
  }, [thread.name, threadSpec, setNodes, setEdges, wfRun])

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
    <DiagramProvider value={{ thread, setThread, selectedNode: node, setSelectedNode: setNode, wfRun }}>
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
            minZoom={0.3}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            snapToGrid={true}
            onMoveEnd={onMoveEnd}
          >
            <Controls />
          </ReactFlow>
          <LayoutManager
            nodeRuns={threadNodeRuns}
            viewportKey={viewportKey}
            setNodes={setNodes}
            onLayoutComplete={applyDefaultSelection}
          />
        </div>
        <Sidebar showNodeRun={wfRun !== undefined} />
      </div>
    </DiagramProvider>
  )
}
