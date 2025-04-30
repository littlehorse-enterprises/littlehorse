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
import { ReactFlow as XYFlow, useEdgesState, useNodesState } from '@xyflow/react'
import { ThreadProvider, ThreadType } from '../context'
import { edgeTypes } from './EdgeTypes'
import { extractEdges } from './EdgeTypes/extractEdges'
import { Layouter } from './NewLayouter'
import nodeTypes from './NodeTypes'
import { extractNodes } from './NodeTypes/extractNodes'
import { ThreadPanel } from './ThreadPanel'
import '@xyflow/react/dist/style.css';


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

export const NewDiagram: FC<Props> = ({ spec, wfRun }) => {
  return <div>NewDiagram</div>
}

export const Diagram: FC<Props> = ({ spec, wfRun }) => {
  // const tenantId = useParams().tenantId as string
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

  // const [edges, setEdges] = useEdgesState(extractEdges(spec, threadSpec))
  // const [nodes, setNodes] = useNodesState(extractNodes(spec, threadSpec))

  // const threadNodeRuns = useMemo(() => {
  //   if (!wfRun) return
  //   return wfRun.threadRuns[thread.number].nodeRuns
  // }, [thread, wfRun])

  // const updateGraph = useCallback(() => {
  //   const nodes = extractNodes(spec, threadSpec)
  //   const edges = extractEdges(spec, threadSpec)
  //   setNodes(nodes)
  //   setEdges(edges)
  // }, [spec.threadSpecs, thread, setNodes, setEdges])

  // useEffect(() => {
  //   updateGraph()
  // }, [updateGraph])

  // const verb =
  //   wfRun?.status === LHStatus.RUNNING
  //     ? 'Stop'
  //     : wfRun?.status === LHStatus.HALTED
  //       ? 'Resume'
  //       : wfRun?.status === LHStatus.ERROR
  //         ? 'Rescue'
  //         : ''

  const initialNodes = extractNodes(spec, threadSpec)
  const initialEdges = extractEdges(spec, threadSpec)
  
  console.log("initialNodes", initialNodes)
  console.log("initialEdges", initialEdges)

  return (
    <Layouter initialNodes={initialNodes} initialEdges={initialEdges} />
  )
}

export type ThreadSpecWithName = {
  name: string
  threadSpec: ThreadSpec
}