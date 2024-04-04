'use client'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import ReactFlow, { Controls, OnConnect, Panel, addEdge, useEdgesState, useNodesState } from 'reactflow'
import 'reactflow/dist/base.css'
import { Layouter } from './Layouter'
import nodeTypes from './NodeTypes'
import { extractEdges } from './extractEdges'
import { extractNodes } from './extractNodes'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { edgeTypes } from './EdgeTypes'
import { ThreadRunWithNodeRuns } from '@/app/(authenticated)/wfRun/[id]/getWfRun'

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

  useEffect(() => {
    setNodes(extractNodes(threadSpec))
    setEdges(extractEdges(threadSpec))
  }, [nodeRuns, setEdges, setNodes, threadSpec])

  const onConnect: OnConnect = useCallback(params => setEdges(eds => addEdge(params, eds)), [setEdges])

  return (
    <div className="mb-4 min-h-[600px] min-w-full rounded border-2 border-slate-100 bg-slate-50 shadow-inner">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodesConnectable={false}
        elementsSelectable
        minZoom={0.3}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        className="min-h-[600px] min-w-full bg-slate-50"
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
      <Layouter nodeRuns={wfRun?.threadRuns[wfRun.greatestThreadrunNumber].nodeRuns} />
    </div>
  )
}
