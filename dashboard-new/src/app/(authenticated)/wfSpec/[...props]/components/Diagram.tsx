'use client'
import { ThreadRunWithNodeRuns } from '@/app/(authenticated)/wfRun/[...ids]/getWfRun'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import ReactFlow, { Controls, Panel, useEdgesState, useNodesState } from 'reactflow'
import 'reactflow/dist/base.css'
import { edgeTypes } from './EdgeTypes'
import { Layouter } from './Layouter'
import nodeTypes from './NodeTypes'
import { extractEdges } from './extractEdges'
import { extractNodes } from './extractNodes'

type Props = {
  wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  nodeRuns?: NodeRun[]
  spec: WfSpec
}

export const Diagram: FC<Props> = ({ spec, wfRun, nodeRuns }) => {
  const currentThread = wfRun
    ? wfRun.threadRuns[wfRun.greatestThreadrunNumber].threadSpecName
    : spec.entrypointThreadName
  const [thread, setThread] = useState(currentThread)

  const threadSpec = useMemo(() => {
    if (thread === undefined) return spec.threadSpecs[spec.entrypointThreadName]
    return spec.threadSpecs[thread]
  }, [spec, thread])
  const [edges, setEdges] = useEdgesState(extractEdges(threadSpec))
  const [nodes, setNodes] = useNodesState(extractNodes(threadSpec))

  const threadRunNumber = useMemo(() => {
    const threadRun = wfRun?.threadRuns.find(threadRun => threadRun.threadSpecName === thread)
    return threadRun?.number
  }, [thread, wfRun?.threadRuns])
  const threadNodeRuns = useMemo(() => {
    if (!wfRun || threadRunNumber === undefined) return
    return wfRun.threadRuns[threadRunNumber].nodeRuns
  }, [threadRunNumber, wfRun])

  const updateGraph = useCallback(() => {
    const nodes = extractNodes(threadSpec)
    const edges = extractEdges(threadSpec)
    setNodes(nodes)
    setEdges(edges)
  }, [setEdges, setNodes, threadSpec])

  useEffect(() => {
    updateGraph()
  }, [updateGraph])

  return (
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
        <Panel position="top-left">
          <div className="flex w-full items-center justify-between gap-2">
            {Object.keys(spec.threadSpecs)
              .reverse()
              .map(threadName => (
                <button
                  className={
                    'border-[1px] p-2 text-sm shadow ' +
                    (threadName === thread ? 'bg-blue-500 text-white' : 'bg-white text-black')
                  }
                  key={threadName}
                  onClick={() => setThread(threadName)}
                >
                  {threadName}
                </button>
              ))}
          </div>
        </Panel>
        <Controls />
      </ReactFlow>
      <Layouter nodeRuns={threadNodeRuns} />
    </div>
  )
}
