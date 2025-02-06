'use client'
import { LHStatus, NodeRun, WfRun, WfSpec } from 'littlehorse-client/proto'
import { ReadonlyURLSearchParams, useParams, useSearchParams } from 'next/navigation'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import ReactFlow, { Controls, useEdgesState, useNodesState } from 'reactflow'
import 'reactflow/dist/base.css'
import { ThreadProvider, ThreadType } from '../context'
import { edgeTypes } from './EdgeTypes'
import { extractEdges } from './EdgeTypes/extractEdges'
import { Layouter } from './Layouter'
import nodeTypes from './NodeTypes'
import { extractNodes } from './NodeTypes/extractNodes'
import { ThreadPanel } from './ThreadPanel'
import { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import { Button } from '@/components/ui/button'
import { PlayCircleIcon, PlayIcon, RotateCcwIcon, StopCircleIcon } from 'lucide-react'
import { stopWfRun } from '@/app/actions/stopWfRun'
import { resumeWfRun } from '@/app/actions/resumeWfRun'
import { rescueWfRun } from '@/app/actions/rescueWfRun'

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

  const threadSpec = useMemo(() => {
    if (thread === undefined) return spec.threadSpecs[spec.entrypointThreadName]
    return spec.threadSpecs[thread.name]
  }, [spec, thread])

  const [edges, setEdges] = useEdgesState(extractEdges(threadSpec))
  const [nodes, setNodes] = useNodesState(extractNodes(threadSpec))

  const threadNodeRuns = useMemo(() => {
    if (!wfRun) return
    return wfRun.threadRuns[thread.number].nodeRuns
  }, [thread, wfRun])

  const updateGraph = useCallback(() => {
    const { name } = thread
    const threadSpec = spec.threadSpecs[name]
    const nodes = extractNodes(threadSpec)
    const edges = extractEdges(threadSpec)
    setNodes(nodes)
    setEdges(edges)
  }, [spec.threadSpecs, thread, setNodes, setEdges])

  useEffect(() => {
    updateGraph()
  }, [updateGraph])

  console.log(wfRun)

  return (
    <ThreadProvider value={{ thread, setThread }}>
      <div className="flex gap-3 justify-between">
        <ThreadPanel spec={spec} wfRun={wfRun} />
        {wfRun && <div>
          {wfRun.status === LHStatus.RUNNING && <Button variant="destructive" className="flex items-center gap-2 font-bold" onClick={async () => {
            await stopWfRun(tenantId, wfRun.id!)
          }}>
            STOP <StopCircleIcon />
          </Button>}
          {wfRun.status === LHStatus.HALTED && (
            <Button className="flex items-center gap-2 bg-green-500 hover:bg-green-600 font-bold" onClick={async () => {
              await resumeWfRun(tenantId, wfRun.id!)
            }}>
              RESUME <PlayCircleIcon />
            </Button>
          )}
          {wfRun.status === LHStatus.ERROR && <Button className="flex items-center gap-2 bg-yellow-500 hover:bg-yellow-600 font-bold" onClick={async () => {
            await rescueWfRun(tenantId, wfRun.id!)
          }}>
            RESCUE <RotateCcwIcon />
          </Button>}
        </div>
        }
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
        <Layouter nodeRuns={threadNodeRuns} nodeRunNameToBeHighlighted={nodeRunNameToBeHighlighted} />
      </div>
    </ThreadProvider >
  )
}
