'use client'
import { ThreadRunWithNodeRuns } from '@/app/(authenticated)/(diagram)/wfRun/[...ids]/getWfRun'
import { NodeRun } from 'littlehorse-client/dist/proto/node_run'
import { WfRun } from 'littlehorse-client/dist/proto/wf_run'
import { WfSpec } from 'littlehorse-client/dist/proto/wf_spec'
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

type Props = {
  wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  nodeRuns?: NodeRun[]
  spec: WfSpec
}

export const Diagram: FC<Props> = ({ spec, wfRun }) => {
  const currentThread = wfRun
    ? wfRun.threadRuns[wfRun.greatestThreadrunNumber].threadSpecName
    : spec.entrypointThreadName
  const [thread, setThread] = useState<ThreadType>({ name: currentThread, number: wfRun?.greatestThreadrunNumber || 0 })

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

  return (
    <ThreadProvider value={{ thread, setThread }}>
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
          <ThreadPanel spec={spec} wfRun={wfRun} />
          <Controls />
        </ReactFlow>
        <Layouter nodeRuns={threadNodeRuns} />
      </div>
    </ThreadProvider>
  )
}
