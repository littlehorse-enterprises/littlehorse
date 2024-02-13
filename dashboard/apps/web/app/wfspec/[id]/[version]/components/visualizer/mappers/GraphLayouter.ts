import type { Edge, Node } from 'reactflow'
import type { ELK as ELKType, ElkExtendedEdge, ElkNode } from 'elkjs/lib/elk.bundled.js'
import type { ElkLabel } from 'elkjs'
import type { Client } from 'nice-grpc/src/client/Client'
import type { ThreadRun, WfRun } from '../../../../../../../littlehorse-public-api/wf_run'
import type { Edge as LHEdge, Node as LHNode, WfSpec } from '../../../../../../../littlehorse-public-api/wf_spec'
import type { LittleHorseDefinition } from '../../../../../../../littlehorse-public-api/service'
import type { NodeRun } from '../../../../../../../littlehorse-public-api/node_run'
import EdgeHandler from './ReactFlowEdgeHandler'
import ELKNodeMapper from './ElkNodeMapper'
import NodeTypeMapper from './NodeTypeMapper'

export interface ReactFlowGraph {
    nodes: ReactFlowNodeWithLHInfo[],
    edges: Edge[]
}

export interface ReactFlowNodeWithLHInfo extends Node {
    lhNode: LHNode,
    positionInThreadRun?: number
}

const defaultThreadSpec = 'entrypoint'

class GraphLayouter {
    private elkJsInstance: ELKType
    private readonly extractLabel: (edge: LHEdge) => (string | null)
    private lhClient: Client<LittleHorseDefinition> | undefined

    constructor(elkJsInstance: ELKType, extractLabel: (edge: LHEdge) => string | null, lhClient?: Client<LittleHorseDefinition>) {
        this.extractLabel = extractLabel
        this.elkJsInstance = elkJsInstance
        this.lhClient = lhClient
    }
    async getLayoutedGraphForWfRun(wfSpec: WfSpec, wfSpecName: string, wfRunId: string, threadRunNumber: number, threadSpec: string = defaultThreadSpec): Promise<ReactFlowGraph> {
        if (this.lhClient === undefined) {
            throw Error('LH Client is needed to get the wfRun graph layout.')
        }

        let threadSpecWithValidValue = threadSpec
        if (wfSpec.threadSpecs[threadSpec] === undefined) {
            threadSpecWithValidValue = defaultThreadSpec
        }

        const wfRun: WfRun = await this.lhClient.getWfRun({ id: wfRunId } as any)

        const threadRunForThreadSpec: ThreadRun | undefined = wfRun
            .threadRuns
            .find(tr => tr.threadSpecName === threadSpecWithValidValue && tr.number === threadRunNumber)

        if (threadRunForThreadSpec === undefined) {
            return Promise.reject(new Error('Not able to to find the corresponding thread run.'))
        }

        const wfRunCurrentPosition: number = threadRunForThreadSpec.currentNodePosition

        const layoutedWfSpec: ReactFlowGraph = await this.getLayoutedGraph(wfSpec, wfSpecName, threadSpecWithValidValue)
        const nodeRuns: NodeRun[] = await this.getNodeRuns(wfRunCurrentPosition, wfRun, threadRunForThreadSpec)

        this.markNodeInWfSpecAsAlreadyExecuted(nodeRuns, layoutedWfSpec)

        return Promise.resolve(layoutedWfSpec)
    }

    private markNodeInWfSpecAsAlreadyExecuted(nodeRuns: NodeRun[], layoutedWfSpec: ReactFlowGraph) {
        nodeRuns.forEach((nodeRun: NodeRun) => {
            const nodeRunName: string = nodeRun.nodeName
            layoutedWfSpec.nodes.forEach(node => {
                if (nodeRunName === node.id) {
                    node.data.nodeHasRun = true
                    node.positionInThreadRun = nodeRun.id?.position
                }
            })
        })
    }

    async getLayoutedGraph(wfSpec: WfSpec, wfSpecName: string, desiredThreadSpec: string = defaultThreadSpec): Promise<ReactFlowGraph> {
        const layoutOptions = {
            'elk.algorithm': 'layered',
            'elk.spacing.nodeNode': '400',
            'spacing.edgeNode': '300',
            'elk.direction': 'DOWN',
            'spacing.nodeNodeBetweenLayers': '160',
            'elk.layered.nodePlacement.strategy': 'SIMPLE'
        }

        let desiredThreadSpecWithValidValue = desiredThreadSpec
        if (wfSpec.threadSpecs[desiredThreadSpec] === undefined) {
            desiredThreadSpecWithValidValue = defaultThreadSpec
        }

        const lhNodes: [string, LHNode][] = Object
            .entries(wfSpec.threadSpecs[desiredThreadSpecWithValidValue].nodes)

        const elkNodes: ElkNode[] = this.mapToElkNodes(lhNodes)
        const elkEdges: ElkExtendedEdge[] = this.mapToElkEdges(lhNodes)

        const graphToBeLayouted = {
            id: wfSpecName,
            layoutOptions,
            children: elkNodes,
            edges: elkEdges,
        }

        return this.elkJsInstance.layout(graphToBeLayouted).then(({ children, edges }) => {
            return {
                nodes: this.mapToReactFlowNodes(children ?? [], lhNodes),
                edges: this.mapToReactFlowEdges(edges ?? [])
            }
        })
    }

    private mapToElkNodes(lhNodes: [string, LHNode][]): ElkNode[] {
        return lhNodes.map((lhNode) => {
            return ELKNodeMapper.fromLHNode(lhNode[0])
        })
    }

    private mapToElkEdges(lhNodes: [string, LHNode][]): ElkExtendedEdge[] {
        let elkEdges: ElkExtendedEdge[] = []

        lhNodes.forEach((lhNode: [string, LHNode]): ElkExtendedEdge[] => {
            const nodeInformation: LHNode = lhNode[1]
            const outgoingEdges: LHEdge[] = nodeInformation.outgoingEdges
            const nodeName: string = lhNode[0]

            if (outgoingEdges.length <= 0) {
                return [] as ElkExtendedEdge[]
            }

            elkEdges = elkEdges.concat(outgoingEdges.map((outgoingEdge: LHEdge) => {
                return {
                    id: `${nodeName}-${outgoingEdge.sinkNodeName}`,
                    sources: [ nodeName ],
                    targets: [ outgoingEdge.sinkNodeName ],
                    labels: [ { text: this.extractLabel(outgoingEdge) } as ElkLabel ]
                }
            }))

            return elkEdges
        })

        return elkEdges
    }

    private mapToReactFlowNodes(elkNodes: ElkNode[], lhNodes: [string, LHNode][]): ReactFlowNodeWithLHInfo[] {
        return elkNodes.map((elkNode: ElkNode) => {
            const nodeType: string | undefined = NodeTypeMapper.map(elkNode.id)
            const elkNodeLabel: ElkLabel = elkNode.labels ? elkNode.labels[0] : { text: '' }

            const currentLHNode = lhNodes.find(lhNode => {
                const nodeName: string = lhNode[0]
                return nodeName === elkNodeLabel
            })

            return {
                id: elkNode.id,
                data: {
                    label: elkNode.labels?.[0],
                    failureHandlers: currentLHNode ? currentLHNode[1].failureHandlers : [],
                    nodeHasRun: false
                },
                position: {
                    x: elkNode.x,
                    y: elkNode.y
                },
                type: nodeType,
                lhNode: currentLHNode?.[1]
            } as ReactFlowNodeWithLHInfo
        })
    }

    private mapToReactFlowEdges(edges: ElkExtendedEdge[]): Edge[] {
        return EdgeHandler.accommodateEdges(edges)
    }

    private async getNodeRuns(wfRunCurrentPosition: number, wfRun: WfRun, threadRunForThreadSpec: ThreadRun): Promise<NodeRun[]> {
        if (this.lhClient === undefined) {
            return Promise.reject(new Error('You need to provide a Little Horse Client.'))
        }

        const nodeRunsPromises: Promise<NodeRun>[] = []

        for (let position = 0; position <= wfRunCurrentPosition; position++) {
            try {
                const nodeRun: Promise<NodeRun> = this.lhClient.getNodeRun({
                    wfRunId: wfRun.id,
                    threadRunNumber: threadRunForThreadSpec.number,
                    position
                } as any)
                nodeRunsPromises.push(nodeRun)
            } catch (error) {
                console.error(`GraphLayouter - Not able to get NodeRun for: wfRunId:${wfRun.id} threadRunNumber:${threadRunForThreadSpec.number} position: ${position}`)
            }
        }
        return Promise.all(nodeRunsPromises)
    }
}

export default GraphLayouter
