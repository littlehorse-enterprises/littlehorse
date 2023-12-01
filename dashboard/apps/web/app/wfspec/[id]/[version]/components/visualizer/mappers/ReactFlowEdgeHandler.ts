import type { ElkExtendedEdge } from 'elkjs'
import type { Edge } from 'reactflow'
import { MarkerType } from 'reactflow'
import EdgeTypesEnum from '../EdgeTypesEnum'

const accommodateEdges = (edges: ElkExtendedEdge[]) => {
    let allConnectionsBetweenNodes: string[] = getAllConnectionsBetweenNodes(edges)

    const START_EDGE_ON_THE_LEFT = 'sourceLeft'
    const START_EDGE_ON_THE_RIGHT = 'sourceRight'
    const END_EDGE_ON_THE_LEFT = 'targetLeft'
    const END_EDGE_ON_THE_RIGHT = 'targetRight'

    return edges.map((edge: ElkExtendedEdge) => {
        const source: string = edge.sources[0]
        const target: string = edge.targets[0]

        let label = ''

        if (edge.labels?.[0] !== undefined ) {
            label = edge.labels[0].text ? edge.labels[0].text : ''
        }


        const baseReactFlowEdge: Edge = {
            id: edge.id,
            source,
            type: EdgeTypesEnum.CustomSmartEdgeType,
            animated: true,
            markerEnd: {
                type: MarkerType.ArrowClosed,
            },
            target,
            label
        }

        let sameNodesOccurrences = 0

        const stillNeedToProcessEdgesFromTheLoop = allConnectionsBetweenNodes.includes(`${source}${target}`) || allConnectionsBetweenNodes.includes(`${target}${source}`)
        const isEdgePartOfALoop = source.includes('NOP') && target.includes('NOP') && (stillNeedToProcessEdgesFromTheLoop)

        if (isEdgePartOfALoop) {
            const isEdgeIncludedInForwardDirection = allConnectionsBetweenNodes.includes(`${source}${target}`)
            const isEdgeIncludedInReverseDirection = allConnectionsBetweenNodes.includes(`${target}${source}`)

            if (isEdgeIncludedInForwardDirection) {
                sameNodesOccurrences += 1
            }

            if (isEdgeIncludedInReverseDirection) {
                sameNodesOccurrences += 1
            }

            const nodesInvolvedIn2Edges = sameNodesOccurrences === 2

            const reactFlowEdge = {
                ...baseReactFlowEdge,
                sourceHandle: nodesInvolvedIn2Edges ? START_EDGE_ON_THE_LEFT : START_EDGE_ON_THE_RIGHT,
                targetHandle: nodesInvolvedIn2Edges ? END_EDGE_ON_THE_LEFT : END_EDGE_ON_THE_RIGHT,
            }

            const connectionsAfterRemovingAlreadyProcessedEdge = allConnectionsBetweenNodes.filter(connection => connection !== `${source}${target}`)
            allConnectionsBetweenNodes = connectionsAfterRemovingAlreadyProcessedEdge
            return reactFlowEdge
        }

        return baseReactFlowEdge
    })
}

const getAllConnectionsBetweenNodes = (edges: ElkExtendedEdge[]) => {
    const allConnectionsBetweenNodes: string[] = []

    edges?.forEach(edge => {
        allConnectionsBetweenNodes.push(`${edge.sources[0]}${edge.targets[0]}`)
    })
    return allConnectionsBetweenNodes
}

const ReactFlowEdgeHandler = {
    accommodateEdges
}

export default ReactFlowEdgeHandler
