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
import { LHStatus, ThreadRun, WfRun, WfSpec } from 'littlehorse-client/proto'
import { PlayCircleIcon, RotateCcwIcon, StopCircleIcon } from 'lucide-react'
import { FC, useCallback, useEffect, useMemo, useState } from 'react'
import ReactFlow, { Controls, useEdgesState, useNodesState } from 'reactflow'
import 'reactflow/dist/base.css'
import { DiagramProvider, NodeInContext, ThreadType } from '../context'
import edgeTypes from './EdgeTypes'
import { extractEdges } from './EdgeTypes/extractEdges'
import { LayoutManager } from './LayoutManager'
import nodeTypes from './NodeTypes'
import { extractNodes } from './NodeTypes/extractNodes'
import { Sidebar } from './Sidebar'
import { ThreadPanel } from './ThreadPanel'

type Props = {
  wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }
  spec: WfSpec
}
const getCycleNodes = (threadSpec: WfSpec['threadSpecs'][string]) => {
  Object.entries(threadSpec.nodes).forEach(([nodeId, node]) => {
    if (node.outgoingEdges.length >= 2) {
      const sourceNum = parseInt(nodeId.split('-')[0])
      node.outgoingEdges.forEach(edge => {
        const targetNum = parseInt(edge.sinkNodeName.split('-')[0])
        if (targetNum <= sourceNum) {
          const targetNodeId = edge.sinkNodeName
          const cycleNodeId = `cycle-${nodeId}-${edge.sinkNodeName}`

          // Create a properly typed cycle node without using `any`/`unknown`.
          type NodeMap = (typeof threadSpec.nodes)[string]
          const cycleNode: NodeMap = {
            outgoingEdges: [
              {
                sinkNodeName: targetNodeId,
                variableMutations: [],
              },
            ],
            failureHandlers: [],
            node: { $case: 'cycle', value: {} } as unknown as NodeMap['node'],
          }

          threadSpec.nodes[cycleNodeId] = cycleNode

          threadSpec.nodes[edge.sinkNodeName].outgoingEdges = threadSpec.nodes[edge.sinkNodeName].outgoingEdges.filter(
            edgeItem => {
              return edgeItem.sinkNodeName !== nodeId
            }
          )
          if (!threadSpec.nodes[nodeId].outgoingEdges.some(e => e.sinkNodeName === cycleNodeId)) {
            threadSpec.nodes[nodeId].outgoingEdges.push({
              sinkNodeName: cycleNodeId,
              variableMutations: [],
            })
          }
          threadSpec.nodes[nodeId].outgoingEdges = threadSpec.nodes[nodeId].outgoingEdges.filter(edgeItem => {
            return edgeItem.sinkNodeName !== edge.sinkNodeName
          })
        }
      })
    }
  })
  return threadSpec
}
export const Diagram: FC<Props> = ({ spec, wfRun }) => {
  const { tenantId } = useWhoAmI()
  const currentThread = wfRun
    ? (wfRun.threadRuns.find(tr => tr.number === wfRun.greatestThreadrunNumber)?.threadSpecName ??
      spec.entrypointThreadName)
    : spec.entrypointThreadName

  const [thread, setThread] = useState<ThreadType>({
    name: currentThread,
    number: wfRun ? wfRun.greatestThreadrunNumber : 0,
  })
  const [node, setNode] = useState<NodeInContext>(undefined)

  const threadSpec = useMemo(() => {
    if (thread === undefined) return spec.threadSpecs[spec.entrypointThreadName]
    return spec.threadSpecs[thread.name]
  }, [spec, thread])
  getCycleNodes(threadSpec)
  console.log(threadSpec)
  const [edges, setEdges] = useEdgesState(extractEdges(threadSpec))
  const [nodes, setNodes] = useNodesState(extractNodes(threadSpec))

  const threadNodeRuns = useMemo(() => {
    if (!wfRun) return
    return (wfRun.threadRuns.find(tr => tr.number === thread.number) as ThreadRun & ThreadRunWithNodeRuns).nodeRuns
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

  const verb =
    wfRun?.status === LHStatus.RUNNING
      ? 'Stop'
      : wfRun?.status === LHStatus.HALTED
        ? 'Resume'
        : wfRun?.status === LHStatus.ERROR
          ? 'Rescue'
          : ''
  return (
    <DiagramProvider value={{ thread, setThread, selectedNode: node, setSelectedNode: setNode }}>
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
      <div className="grid min-h-[600px] grid-cols-[1fr_330px]">
        <div className="mb-4 flex-1 rounded border-2 border-slate-100 bg-slate-50 shadow-inner">
          <ReactFlow
            nodes={nodes}
            edges={edges}
            nodesConnectable={false}
            nodesDraggable={false}
            elementsSelectable
            onlyRenderVisibleElements={true}
            minZoom={0.3}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            snapToGrid={true}
          >
            <Controls />
          </ReactFlow>
          <LayoutManager nodeRuns={threadNodeRuns} />
        </div>
        <Sidebar showNodeRun={wfRun !== undefined} />
      </div>
    </DiagramProvider>
  )
}
