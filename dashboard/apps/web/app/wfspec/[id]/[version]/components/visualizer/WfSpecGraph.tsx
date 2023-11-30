import type { MouseEvent as ReactMouseEvent } from 'react'
import { useEffect, useMemo, useState } from 'react'
import type { Edge } from 'reactflow'
import { ReactFlow } from 'reactflow'
import 'reactflow/dist/style.css'
import { SmartBezierEdge, SmartStepEdge } from '@tisoap/react-flow-smart-edge'
import EntrypointNodeType from './nodeTypes/EntrypointNodeType'
import ExitNodeType from './nodeTypes/ExitNodeType'
import NopNodeType from './nodeTypes/NopNodeType'
import TaskNodeType from './nodeTypes/TaskNodeType'
import SpawnThreadNodeType from './nodeTypes/SpawnThreadNodeType'
import WaitForThreadsNodeType from './nodeTypes/WaitForThreadsNodeType'
import EdgeTypesEnum from './EdgeTypesEnum'
import ExternalEventNodeType from './nodeTypes/ExternalEventNodeType'
import SleepNodeType from './nodeTypes/SleepNodeType'
import SpawnMultipleThreadsNodeType from './nodeTypes/SpawnMultipleThreadsNodeType'
import UserTaskNodeType from './nodeTypes/UserTaskNodeType'
import { CustomSmartEdgeType } from './edgeTypes/CustomSmartEdgeType'
import type { ReactFlowNodeWithLHInfo } from './mappers/GraphLayouter'

interface WfSpecGraphProps {
    wfSpecName: string | undefined,
    wfSpecMajorVersion: number,
    wfSpecRevision: number,
    setSelectedNodeName: any,
    threadSpec: string,
    threadRunNumber: number | null,
    isWfSpecVisualization: boolean,
    wfRunId: string | null,
    setGraphWithNodeRunPosition: any

}

export function WfSpecGraph({
    wfSpecName,
    wfSpecMajorVersion,
    wfSpecRevision,
    setSelectedNodeName,
    threadSpec,
    threadRunNumber,
    isWfSpecVisualization,
    wfRunId,
    setGraphWithNodeRunPosition
}: WfSpecGraphProps) {
    const nodeTypes = useMemo(() => ({
        'entrypointNodeType': EntrypointNodeType,
        'taskNodeType': TaskNodeType,
        'nopNodeType': NopNodeType,
        'exitNodeType': ExitNodeType,
        'spawnThreadNodeType': SpawnThreadNodeType,
        'waitForThreadsNodeType': WaitForThreadsNodeType,
        'externalEventNodeType': ExternalEventNodeType,
        'sleepNodeType': SleepNodeType,
        'spawnMultipleThreadsNodeType': SpawnMultipleThreadsNodeType,
        'userTaskNodeType': UserTaskNodeType
    }), [])

    const edgeTypes = useMemo(() => ({
        [EdgeTypesEnum.SmartEdgeType]: SmartBezierEdge,
        [EdgeTypesEnum.CustomSmartEdgeType]: CustomSmartEdgeType,
        [EdgeTypesEnum.SmartStepType]: SmartStepEdge
    }), [])

    const [ reactFlowGraphLayouted, setReactFlowGraphLayouted ] = useState({
        nodes: [] as ReactFlowNodeWithLHInfo[],
        edges: [] as Edge[]
    })

    const getLayoutedGraph = async () => {
        if (wfSpecMajorVersion !== undefined  && wfSpecRevision !== undefined && wfSpecName !== undefined && threadSpec !== undefined) {
            const layoutedGraphResponse = await fetch('/api/visualization/workflowLayoutedGraph', {
                method: 'POST',
                body: JSON.stringify({
                    wfSpecName,
                    majorVersion: wfSpecMajorVersion,
                    revision: wfSpecRevision,
                    threadSpec,
                    threadRunNumber,
                    isWfSpecVisualization,
                    wfRunId
                })
            })

            if (layoutedGraphResponse.ok) {
                layoutedGraphResponse.json().then((layoutedGraph) => {
                    if (layoutedGraph !== undefined && layoutedGraph.nodes !== undefined) {
                        layoutedGraph.nodes = layoutedGraph.nodes.map(node => {
                            return { ...node, data: { ...node.data, isWfSpecVisualization } }
                        })

                        setReactFlowGraphLayouted(layoutedGraph)
                        setGraphWithNodeRunPosition(layoutedGraph)
                    }
                })
            }
        }
    }

    useEffect(() => {
        getLayoutedGraph()
    }, [ wfSpecName, wfSpecRevision, wfSpecMajorVersion, threadSpec, threadRunNumber ])

    const onNodeClick = (_: ReactMouseEvent, node: ReactFlowNodeWithLHInfo) => {
        setSelectedNodeName(node.id)
    }

    return (
        <div style={{ width: '100vw', height: '100vh' }}>
            <ReactFlow edgeTypes={edgeTypes}
                edges={reactFlowGraphLayouted.edges}
                nodeTypes={nodeTypes}
                nodes={reactFlowGraphLayouted.nodes}
                onNodeClick={onNodeClick}/>
        </div>
    )

}
