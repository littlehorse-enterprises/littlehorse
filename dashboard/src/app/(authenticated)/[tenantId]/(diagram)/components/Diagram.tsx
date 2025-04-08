'use client'
import { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import { rescueWfRun } from '@/app/actions/rescueWfRun'
import { resumeWfRun } from '@/app/actions/resumeWfRun'
import { stopWfRun } from '@/app/actions/stopWfRun'
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'
import { LHStatus, NodeRun, ThreadSpec, WfRun, WfSpec } from 'littlehorse-client/proto'
import { PlayCircleIcon, RotateCcwIcon, StopCircleIcon } from 'lucide-react'
import { ReadonlyURLSearchParams, useParams, useSearchParams } from 'next/navigation'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import ReactFlow, { Controls, useEdgesState, useNodesState } from 'reactflow'
import 'reactflow/dist/base.css'
import { ThreadProvider, ThreadType } from '../context'
import { edgeTypes } from './EdgeTypes'
import { extractAllEdges } from './EdgeTypes/extractEdges'
import { Layouter } from './Layouter'
import nodeTypes from './NodeTypes'
import { extractAllNodes } from './NodeTypes/extractNodes'
import { ThreadPanel } from './ThreadPanel'

type Props = {
  wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  nodeRuns?: NodeRun[]
  spec: WfSpec
}

const isValidThreadRunNumberForCurrentWfRun = (threadRunNumber: number, wfRun: WfRun) => {
  return !isNaN(threadRunNumber) && threadRunNumber >= 0 && wfRun && threadRunNumber <= wfRun.greatestThreadrunNumber
}

const determineDefaultThreadRun = (
  currentThread: string,
  wfRun: (WfRun & { threadRuns: ThreadRunWithNodeRuns[] }) | undefined,
  threadRunNumberFromRedirection: number,
  spec: WfSpec
) => {
  let threadToShowByDefault = { name: currentThread, number: wfRun?.greatestThreadrunNumber || 0 }

  if (wfRun && isValidThreadRunNumberForCurrentWfRun(threadRunNumberFromRedirection, wfRun)) {
    const threadRunName = wfRun
      ? wfRun.threadRuns[threadRunNumberFromRedirection].threadSpecName
      : spec.entrypointThreadName
    threadToShowByDefault = { name: threadRunName, number: threadRunNumberFromRedirection }
  }
  return threadToShowByDefault
}

export const Diagram: FC<Props> = ({ spec, wfRun }) => {
  const tenantId = useParams().tenantId as string
  const currentThread = wfRun
    ? wfRun.threadRuns[wfRun.greatestThreadrunNumber].threadSpecName
    : spec.entrypointThreadName

  const searchParams: ReadonlyURLSearchParams = useSearchParams()
  const threadRunNumberFromRedirection = Number(searchParams.get('threadRunNumber'))
  const nodeRunNameToBeHighlighted = searchParams.get('nodeRunName')!

  let threadToShowByDefault = determineDefaultThreadRun(currentThread, wfRun, threadRunNumberFromRedirection, spec)

  const [thread, setThread] = useState<ThreadType>(threadToShowByDefault)

  const threadSpec: ThreadSpecWithName = useMemo(() => {
    if (thread === undefined) return { name: spec.entrypointThreadName, threadSpec: spec.threadSpecs[spec.entrypointThreadName] }
    return { name: thread.name, threadSpec: spec.threadSpecs[thread.name] }
  }, [spec, thread])

  const [edges, setEdges] = useEdgesState(extractAllEdges(spec, threadSpec))
  const [nodes, setNodes] = useNodesState(extractAllNodes(spec, threadSpec))

  // const threadNodeRuns = useMemo(() => {
  //   if (!wfRun) return
  //   return wfRun.threadRuns[thread.number].nodeRuns
  // }, [thread, wfRun])

  const updateGraph = useCallback(() => {
    const nodes = extractAllNodes(spec, threadSpec)
    const edges = extractAllEdges(spec, threadSpec)
    setNodes(nodes)
    setEdges(edges)
  }, [spec.threadSpecs, thread, setNodes, setEdges])

  useEffect(() => {
    updateGraph()
  }, [updateGraph])

  const verb =
    wfRun?.status === LHStatus.RUNNING
      ? 'Stop'
      : wfRun?.status === LHStatus.HALTED
        ? 'Resume'
        : wfRun?.status === LHStatus.ERROR
          ? 'Rescue'
          : ''

  return (
    <ThreadProvider value={{ thread, setThread }}>
      <div className="flex justify-between gap-3">
        <ThreadPanel spec={spec} wfRun={wfRun} />
        {wfRun && (
          <div>
            <AlertDialog>
              {wfRun.status === LHStatus.RUNNING && (
                <AlertDialogTrigger asChild>
                  <Button variant="destructive" className="flex items-center gap-2 font-bold">
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
                  <AlertDialogAction
                    onClick={async () => {
                      if (wfRun.status === LHStatus.RUNNING) {
                        await stopWfRun(tenantId, wfRun.id!)
                      } else if (wfRun.status === LHStatus.HALTED) {
                        await resumeWfRun(tenantId, wfRun.id!)
                      } else if (wfRun.status === LHStatus.ERROR) {
                        await rescueWfRun(tenantId, wfRun.id!)
                      }
                    }}
                  >
                    Confirm
                  </AlertDialogAction>
                </AlertDialogFooter>
              </AlertDialogContent>
            </AlertDialog>
          </div>
        )}
      </div>
      <div className="mb-4 min-h-[800px] min-w-full rounded border-2 border-slate-100 bg-slate-50 shadow-inner">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodesConnectable={false}
          elementsSelectable
          minZoom={0.3}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          snapToGrid={true}
          className="min-h-[800px] min-w-full bg-slate-50"
        >
          <Controls />
        </ReactFlow>
        <Layouter wfRun={wfRun} nodeRunNameToBeHighlighted={nodeRunNameToBeHighlighted} />
      </div>
    </ThreadProvider>
  )
}

export type ThreadSpecWithName = {
  name: string
  threadSpec: ThreadSpec
}