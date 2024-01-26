import type { Edge, Node } from 'reactflow'
import { MarkerType } from 'reactflow'
import ElkConstructor from 'elkjs/lib/elk.bundled.js'
import type { ExitNode, UserTaskNode, WfSpec } from '../../../../../../../littlehorse-public-api/wf_spec'
import { FailureHandlerDef_LHFailureType, Node as LHNode, WfRunVariableAccessLevel } from '../../../../../../../littlehorse-public-api/wf_spec'
import {
    LHStatus,
    MetadataStatus,
    VariableType,
    WaitForThreadsPolicy
} from '../../../../../../../littlehorse-public-api/common_enums'
import type {
    TaskNode,
    VariableMutation } from '../../../../../../../littlehorse-public-api/common_wfspec'
import {
    Comparator,
    VariableMutationType
} from '../../../../../../../littlehorse-public-api/common_wfspec'
import EdgeLabelExtractor from '../extractors/EdgeLabelExtractor'
import type { WfRun } from '../../../../../../../littlehorse-public-api/wf_run'
import { ThreadType } from '../../../../../../../littlehorse-public-api/wf_run'
import { NodeRun } from '../../../../../../../littlehorse-public-api/node_run'
import LHClient from '../../../../../../../pages/api/LHClient'
import type { ReactFlowGraph, ReactFlowNodeWithLHInfo } from './GraphLayouter'
import GraphLayouter from './GraphLayouter'

jest.mock('../../../../../../../pages/api/LHClient')

const elk = new ElkConstructor()
const labelExtractor = EdgeLabelExtractor.extract
const wfSpecName = '123'

describe('Layouting graph from LH Nodes', () => {
    describe('mapping WfSpec nodes from proto to react flow structure', () => {
        it('should return a graph for the entrypoint thread spec if the provided one was not found', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName, 'invalid-thread-spec')

            expect(wfSpecInReactFlowFormat.nodes[0].lhNode).toEqual(LHNode.fromJSON(
                {
                    'outgoingEdges': [],
                    'variableMutations': [],
                    'failureHandlers': [],
                    'entrypoint': {}
                }
            )
            )
        })

        it('should return a graph for the entrypoint thread spec when thread spec was not provided at all', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)

            expect(wfSpecInReactFlowFormat.nodes[0].lhNode).toEqual(LHNode.fromJSON(
                {
                    'outgoingEdges': [],
                    'variableMutations': [],
                    'failureHandlers': [],
                    'entrypoint': {}
                }
            )
            )
        })

        it('should return the graph for the provided threadSpec', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status': MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    },
                    'another-thread-spec': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-exit-EXIT': {
                                'outgoingEdges': [],

                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const expectedNodesForAnotherThreadSpec: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-exit-EXIT'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '1-exit-EXIT',
                    data: { label: '1-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'exit': {}
                    })
                }
            ]

            const expectedEdges: Edge[] = [
                {
                    'animated': true,
                    'id': '0-entrypoint-ENTRYPOINT-1-exit-EXIT',
                    'label': '',
                    'markerEnd': {
                        'type': MarkerType.ArrowClosed,
                    },
                    'source': '0-entrypoint-ENTRYPOINT',
                    'target': '1-exit-EXIT',
                    'type': 'CUSTOM_SMART_EDGE_TYPE',
                }
            ]

            const desiredThreadSpec = 'another-thread-spec'
            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName, desiredThreadSpec)

            expect(wfSpecInReactFlowFormat.nodes).toEqual(expectedNodesForAnotherThreadSpec)
            expect(wfSpecInReactFlowFormat.edges).toEqual(expectedEdges)


        })

        it('each node should have its LHNode information', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)

            expect(wfSpecInReactFlowFormat.nodes[0].lhNode).toEqual(LHNode.fromJSON(
                {
                    'outgoingEdges': [],
                    'variableMutations': [],
                    'failureHandlers': [],
                    'entrypoint': {}
                }
            )
            )
        })

        it('should map an entry point with no edges when a single threadSpec was provided', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const expectedNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                }
            ]

            const expectedEdges: Edge[] = []

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)
            const nodes: ReactFlowNodeWithLHInfo[] = wfSpecInReactFlowFormat.nodes
            const edges: Edge[] = wfSpecInReactFlowFormat.edges

            expect(wfSpecInReactFlowFormat.nodes.length).toEqual(1)
            expect(nodes).toEqual(expectedNodes)
            expect(wfSpecInReactFlowFormat.edges.length).toEqual(0)
            expect(edges).toEqual(expectedEdges)
        })

        it('should map an entry point with an edge', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-exit-EXIT',
                                        'variableMutations': [],

                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const expectedNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-exit-EXIT'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '1-exit-EXIT',
                    data: { label: '1-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'exit': {}
                    })
                }
            ]

            const expectedEdges: Edge[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT-1-exit-EXIT',
                    source: '0-entrypoint-ENTRYPOINT',
                    target: '1-exit-EXIT',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                }
            ]

            const mapper = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)
            const nodes: Node[] = wfSpecInReactFlowFormat.nodes
            const edges: Edge[] = wfSpecInReactFlowFormat.edges

            expect(wfSpecInReactFlowFormat.nodes.length).toEqual(2)
            expect(nodes).toEqual(expectedNodes)
            expect(wfSpecInReactFlowFormat.edges.length).toEqual(1)
            expect(edges).toEqual(expectedEdges)
        })

        it('should map nodes that have edges each one', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                        'variableMutations': []
                                    }
                                ],

                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-evaluate-risk-of-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],

                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '2-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const expectedNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-evaluate-risk-of-decision-TASK'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK',
                    data: { label: '1-evaluate-risk-of-decision-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '2-exit-EXIT'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '2-exit-EXIT',
                    data: { label: '2-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'exit': {}
                    })
                },
            ]

            const expectedEdges: Edge[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT-1-evaluate-risk-of-decision-TASK',
                    source: '0-entrypoint-ENTRYPOINT',
                    target: '1-evaluate-risk-of-decision-TASK',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK-2-exit-EXIT',
                    source: '1-evaluate-risk-of-decision-TASK',
                    target: '2-exit-EXIT',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                }
            ]

            const mapper = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)
            const nodes: Node[] = wfSpecInReactFlowFormat.nodes
            const edges: Edge[] = wfSpecInReactFlowFormat.edges

            expect(wfSpecInReactFlowFormat.nodes.length).toEqual(3)
            expect(nodes).toEqual(expectedNodes)
            expect(wfSpecInReactFlowFormat.edges.length).toEqual(2)
            expect(edges).toEqual(expectedEdges)
        })

        it('should map nodes that have more than one edge', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                frozenVariables: [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                        'variableMutations': [],
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-evaluate-risk-of-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-nop-NOP',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'was-it-a-risky-decision',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {}
                                            }
                                        ],
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'evaluate-risk-of-decision',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        }
                                    ]
                                }
                            },
                            '2-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-save-risk-evaluation-results-TASK',
                                        'condition': {
                                            'comparator': Comparator.EQUALS,
                                            'left': {
                                                'variableName': 'was-it-a-risky-decision'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'bool': false
                                                }
                                            }
                                        },
                                        'variableMutations': [],
                                    },
                                    {
                                        'sinkNodeName': '5-fraud-detection-fraud-form-2-USER_TASK',
                                        'condition': {
                                            'comparator': Comparator.NOT_EQUALS,
                                            'left': {
                                                'variableName': 'was-it-a-risky-decision'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'bool': false
                                                }
                                            }
                                        },
                                        'variableMutations': [],
                                    }
                                ],

                                'failureHandlers': [],
                                'nop': {}
                            },
                            '3-save-risk-evaluation-results-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '4-nop-NOP',
                                        'variableMutations': [],
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'save-risk-evaluation-results',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        },
                                        {
                                            'variableName': 'was-it-a-risky-decision'
                                        }
                                    ]
                                }
                            },
                            '4-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '7-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '5-fraud-detection-fraud-form-2-USER_TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '6-save-risk-evaluation-results-TASK',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'is-risk-approved',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {
                                                    'jsonpath': '$.isApproved'
                                                }
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'userTask': {
                                    'userTaskDefName': 'fraud-detection-fraud-form-2',
                                    'userGroup': {
                                        'literalValue': {
                                            'str': 'fraud-detection'
                                        }
                                    },
                                    'actions': [],
                                    'userTaskDefVersion': 2
                                }
                            },
                            '6-save-risk-evaluation-results-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '4-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'save-risk-evaluation-results',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        },
                                        {
                                            'variableName': 'is-risk-approved'
                                        }
                                    ]
                                }
                            },
                            '7-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.STR,
                                    'name': 'request-id',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.BOOL,
                                    'name': 'was-it-a-risky-decision',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.BOOL,
                                    'name': 'is-risk-approved',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            }
                        ],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }


            const expectedNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'entrypoint': {}
                    } as LHNode
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK',
                    data: { label: '1-evaluate-risk-of-decision-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '2-nop-NOP',
                                'variableMutations': [
                                    {
                                        'lhsName': 'was-it-a-risky-decision',
                                        'operation': VariableMutationType.ASSIGN,
                                        nodeOutput: {}
                                    }
                                ] as VariableMutation[]
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name: 'evaluate-risk-of-decision'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                }
                            ]
                        } as TaskNode,
                    } as LHNode
                },
                {
                    id: '2-nop-NOP',
                    data: { label: '2-nop-NOP', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    lhNode:{
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '3-save-risk-evaluation-results-TASK',
                                'condition': {
                                    'comparator': Comparator.EQUALS,
                                    'left': {
                                        'variableName': 'was-it-a-risky-decision'
                                    },
                                    'right': {
                                        literalValue: {
                                            'bool': false
                                        }
                                    }
                                },
                                'variableMutations': []
                            },
                            {
                                'sinkNodeName': '5-fraud-detection-fraud-form-2-USER_TASK',
                                'condition': {
                                    'comparator': Comparator.NOT_EQUALS,
                                    'left': {
                                        'variableName': 'was-it-a-risky-decision'
                                    },
                                    'right': {
                                        literalValue: {
                                            'bool': false
                                        }
                                    }
                                },
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'nop': {}
                    } as LHNode
                },
                {
                    id: '3-save-risk-evaluation-results-TASK',
                    data: { label: '3-save-risk-evaluation-results-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '4-nop-NOP',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name: 'save-risk-evaluation-results'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                },
                                {
                                    'variableName': 'was-it-a-risky-decision'
                                }
                            ]
                        } as TaskNode
                    } as LHNode
                },
                {
                    id: '4-nop-NOP',
                    data: { label: '4-nop-NOP', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '7-exit-EXIT',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'nop': {}
                    } as LHNode
                },
                {
                    id: '5-fraud-detection-fraud-form-2-USER_TASK',
                    data: { label: '5-fraud-detection-fraud-form-2-USER_TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'userTaskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '6-save-risk-evaluation-results-TASK',
                                'variableMutations': [
                                    {
                                        'lhsName': 'is-risk-approved',
                                        'operation': VariableMutationType.ASSIGN,
                                        'nodeOutput': {
                                            'jsonpath': '$.isApproved'
                                        }
                                    }
                                ]
                            }
                        ],
                        'failureHandlers': [],
                        'userTask': {
                            'userTaskDefName': 'fraud-detection-fraud-form-2',
                            'userGroup': {
                                'literalValue': {
                                    'str': 'fraud-detection'
                                }
                            },
                            'actions': [],
                            'userTaskDefVersion': 2
                        } as UserTaskNode
                    } as LHNode
                },
                {
                    id: '6-save-risk-evaluation-results-TASK',
                    data: { label: '6-save-risk-evaluation-results-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '4-nop-NOP',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            'taskDefId': {
                                'name': 'save-risk-evaluation-results',
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                },
                                {
                                    'variableName': 'is-risk-approved'
                                }
                            ]
                        }
                    } as LHNode
                },
                {
                    id: '7-exit-EXIT',
                    data: { label: '7-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: {
                        'outgoingEdges': [],
                        'failureHandlers': [],
                        'exit': {} as ExitNode
                    } as LHNode
                }
            ]

            const expectedEdges: Edge[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT-1-evaluate-risk-of-decision-TASK',
                    source: '0-entrypoint-ENTRYPOINT',
                    target: '1-evaluate-risk-of-decision-TASK',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK-2-nop-NOP',
                    source: '1-evaluate-risk-of-decision-TASK',
                    target: '2-nop-NOP',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '2-nop-NOP-3-save-risk-evaluation-results-TASK',
                    source: '2-nop-NOP',
                    target: '3-save-risk-evaluation-results-TASK',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: 'was-it-a-risky-decision = false'
                },
                {
                    id: '2-nop-NOP-5-fraud-detection-fraud-form-2-USER_TASK',
                    source: '2-nop-NOP',
                    target: '5-fraud-detection-fraud-form-2-USER_TASK',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: 'was-it-a-risky-decision != false'
                },
                {
                    id: '3-save-risk-evaluation-results-TASK-4-nop-NOP',
                    source: '3-save-risk-evaluation-results-TASK',
                    target: '4-nop-NOP',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '4-nop-NOP-7-exit-EXIT',
                    source: '4-nop-NOP',
                    target: '7-exit-EXIT',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '5-fraud-detection-fraud-form-2-USER_TASK-6-save-risk-evaluation-results-TASK',
                    source: '5-fraud-detection-fraud-form-2-USER_TASK',
                    target: '6-save-risk-evaluation-results-TASK',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                },
                {
                    id: '6-save-risk-evaluation-results-TASK-4-nop-NOP',
                    source: '6-save-risk-evaluation-results-TASK',
                    target: '4-nop-NOP',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    label: ''
                }
            ]

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)
            const nodes: Node[] = wfSpecInReactFlowFormat.nodes
            const edges: Edge[] = wfSpecInReactFlowFormat.edges

            expect(wfSpecInReactFlowFormat.nodes.length).toEqual(8)
            expect(nodes).toEqual(expectedNodes)
            expect(wfSpecInReactFlowFormat.edges.length).toEqual(8)
            expect(edges).toEqual(expectedEdges)
        })

        it('should layout the graph in vertical position having nodes that dont overlap with each other', async () => {
            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-evaluate-risk-of-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '2-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-save-risky-decision-TASK',
                                        'variableMutations': []
                                    },
                                    {
                                        'sinkNodeName': '4-save-not-risky-decision-TASK',
                                        'variableMutations': []
                                    }
                                ],

                                'failureHandlers': [],
                                'nop': {}
                            },
                            '3-save-risky-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '5-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': []
                            },
                            '4-save-not-risky-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '5-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': []
                            },
                            '5-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '6-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '6-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const expectedReactFlowNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'entrypoint': {}
                    }
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK',
                    data: { label: '1-evaluate-risk-of-decision-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '2-nop-NOP'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '2-nop-NOP',
                    data: { label: '2-nop-NOP', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '3-save-risky-decision-TASK',
                                'variableMutations': []
                            },
                            {
                                'sinkNodeName': '4-save-not-risky-decision-TASK',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'nop': {}
                    }
                },
                {
                    id: '3-save-risky-decision-TASK',
                    data: { label: '3-save-risky-decision-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '5-nop-NOP'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': []
                    })
                },
                {
                    id: '4-save-not-risky-decision-TASK',
                    data: { label: '4-save-not-risky-decision-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '5-nop-NOP'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': []
                    })
                },
                {
                    id: '5-nop-NOP',
                    data: { label: '5-nop-NOP', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '6-exit-EXIT'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'nop': {}
                    })
                },
                {
                    id: '6-exit-EXIT',
                    data: { label: '6-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'exit': {}
                    })
                },
            ]

            const layoutOptions = {
                'elk.algorithm': 'layered',
                'elk.spacing.nodeNode': '400',
                'spacing.edgeNode': '300',
                'elk.direction': 'DOWN',
                'spacing.nodeNodeBetweenLayers': '160',
                'elk.layered.nodePlacement.strategy': 'SIMPLE'
            }

            const elkInstance = new ElkConstructor()
            const elkSpyOnLayout = jest.spyOn(elkInstance, 'layout')

            const mapper = new GraphLayouter(elkInstance, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)

            const argsSendToLayoutLib = elkSpyOnLayout.mock.calls[0][0]

            expect(argsSendToLayoutLib.id).toEqual(wfSpecName)
            expect(argsSendToLayoutLib.layoutOptions).toEqual(layoutOptions)

            const nodes: Node[] = wfSpecInReactFlowFormat.nodes

            expect(nodes).toEqual(expectedReactFlowNodes)
        })
        it('should set failure handlers property on node if present', async () => {
            const wfSpec: WfSpec = {
                id: {
                    'name': 'example-exception-handler',
                    'majorVersion': 0,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-17T17:02:53.770Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-fail-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-fail-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-my-task-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [
                                    {
                                        'handlerSpecName': 'exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR',
                                        /*
                       eslint-disable-next-line camelcase
                       */
                                        'anyFailureOfType': FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR
                                    }
                                ],
                                'task': {
                                    'taskDefId': {
                                        'name': 'fail',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': []
                                }
                            },
                            '2-my-task-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'my-task',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': []
                                }
                            },
                            '3-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    },
                    'exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-my-task-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-my-task-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'my-task',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': []
                                }
                            },
                            '2-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)

            const nodes: Node[] = wfSpecInReactFlowFormat.nodes

            const nodeWithFailureHandler = nodes.find(node => node.data.label === '1-fail-TASK')

            expect(nodeWithFailureHandler?.data.failureHandlers).toEqual([ {
                'handlerSpecName': 'exn-handler-1-fail-TASK-FAILURE_TYPE_ERROR',
                /*
           eslint-disable-next-line camelcase
           */
                'anyFailureOfType': FailureHandlerDef_LHFailureType.FAILURE_TYPE_ERROR
            } ])
        })

        it('should map a while loop flow edges correctly positioned including their labels', async () => {
            const wfSpec: WfSpec = {
                id: {
                    'name': 'example-conditionals-while',
                    'majorVersion': 0,
                    'revision': 0,
                },
                'frozenVariables': [],
                'createdAt': '2023-10-16T16:37:18.380Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-eating-donut-TASK',
                                        'condition': {
                                            'comparator': Comparator.GREATER_THAN,
                                            'left': {
                                                'variableName': 'number-of-donuts'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'int': 0
                                                }
                                            }
                                        },
                                        'variableMutations': []
                                    },
                                    {
                                        'sinkNodeName': '3-nop-NOP',
                                        'condition': {
                                            'comparator': Comparator.LESS_THAN_EQ,
                                            'left': {
                                                'variableName': 'number-of-donuts'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'int': 0
                                                }
                                            }
                                        },
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '2-eating-donut-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-nop-NOP',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'number-of-donuts',
                                                'operation': VariableMutationType.SUBTRACT,
                                                'literalValue': {
                                                    'int': 1
                                                }
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'eating-donut',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'number-of-donuts'
                                        }
                                    ]
                                }
                            },
                            '3-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-nop-NOP',
                                        'condition': {
                                            'comparator': Comparator.GREATER_THAN,
                                            'left': {
                                                'variableName': 'number-of-donuts'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'int': 0
                                                }
                                            }
                                        },
                                        'variableMutations': []
                                    },
                                    {
                                        'sinkNodeName': '4-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '4-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [
                            {
                                'varDef': {
                                    'type': VariableType.INT,
                                    'name': 'number-of-donuts',
                                    'defaultValue': undefined,
                                },
                                'jsonIndexes': [],
                                required: true,
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                searchable: true
                            }
                        ],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor)
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraph(wfSpec, wfSpecName)

            const expectedEdges = [
                {
                    id: '0-entrypoint-ENTRYPOINT-1-nop-NOP',
                    source: '0-entrypoint-ENTRYPOINT',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '1-nop-NOP',
                    label: ''
                },
                {
                    id: '1-nop-NOP-2-eating-donut-TASK',
                    source: '1-nop-NOP',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '2-eating-donut-TASK',
                    label: 'number-of-donuts > 0'
                },
                {
                    id: '1-nop-NOP-3-nop-NOP',
                    source: '1-nop-NOP',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '3-nop-NOP',
                    sourceHandle: 'sourceLeft',
                    targetHandle: 'targetLeft',
                    label: 'number-of-donuts <= 0'
                },
                {
                    id: '2-eating-donut-TASK-3-nop-NOP',
                    source: '2-eating-donut-TASK',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '3-nop-NOP',
                    label: ''
                },
                {
                    id: '3-nop-NOP-1-nop-NOP',
                    source: '3-nop-NOP',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '1-nop-NOP',
                    sourceHandle: 'sourceRight',
                    targetHandle: 'targetRight',
                    label: 'number-of-donuts > 0'
                },
                {
                    id: '3-nop-NOP-4-exit-EXIT',
                    source: '3-nop-NOP',
                    type: 'CUSTOM_SMART_EDGE_TYPE',
                    animated: true,
                    markerEnd: { type: MarkerType.ArrowClosed },
                    target: '4-exit-EXIT',
                    label: ''
                }
            ]

            expect(wfSpecInReactFlowFormat.edges).toEqual(expectedEdges)
        })
    })

    describe('layout WfRuns highlighting the executed nodes from the wfRun', () => {
        beforeEach(() => {
            jest.clearAllMocks()
        })

        it('when wfRun is in ERROR status and we are not able to get the latest nodeRun, that nodeRun is marked as it has not run', async () => {
            const WF_RUN_ID = '61e8c3cdba664d929c3c99d2d1a4e91a'

            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [ {
                                    'sinkNodeName': '8-exit-EXIT',
                                    'variableMutations': []
                                } ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '8-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const wfRun: WfRun = {
                'id': {
                    'id': WF_RUN_ID
                },
                'wfSpecId': {
                    'name': 'example-interrupt-handler',
                    'majorVersion': 0 ,
                    'revision': 0
                },
                'greatestThreadrunNumber': 0,
                'oldWfSpecVersions': [],
                'status': LHStatus.ERROR,
                'startTime': '2023-10-26T19:26:59.700Z',
                'endTime': '2023-10-26T19:26:59.701Z',
                'threadRuns': [
                    {
                        'number': 0,
                        'wfSpecId': {
                            'name': 'example-interrupt-handler',
                            'majorVersion': 0 ,
                            'revision': 0
                        },
                        'status': LHStatus.ERROR,
                        'threadSpecName': 'entrypoint',
                        'startTime': '2023-10-26T19:26:59.701Z',
                        'endTime': '2023-10-26T19:26:59.701Z',
                        'errorMessage': 'Failed calculating maturation for timer: Cant convert NULL to INT',
                        'childThreadIds': [],
                        'haltReasons': [],
                        'currentNodePosition': 1,
                        'handledFailedChildren': [],
                        'type': ThreadType.ENTRYPOINT
                    }
                ],
                'pendingInterrupts': [],
                'pendingFailures': []
            }

            const spy = jest.spyOn(LHClient, 'getInstance')
            spy.mockImplementationOnce(() => ({
                getWfRun: (_: any): Promise<WfRun> => {
                    return Promise.resolve(wfRun)
                },
                getNodeRun: (getNodeRunRequest: any): Promise<NodeRun> => {
                    if (getNodeRunRequest.position === 0) {
                        return Promise.resolve(NodeRun.fromJSON({
                            'wfRunId': '56021fd8a2054563b25b595e6162b00c',
                            'threadRunNumber': 1,
                            'position': 0,
                            'status': 'COMPLETED',
                            'arrivalTime': '2023-10-24T16:56:36.191Z',
                            'endTime': '2023-10-24T16:56:36.191Z',
                            'wfSpecId': {
                                'name': 'example-child-thread',
                                'version': 0
                            },
                            'threadSpecName': 'entrypoint',
                            'nodeName': '0-entrypoint-ENTRYPOINT',
                            'failures': [],
                            'entrypoint': {},
                            'failureHandlerIds': []
                        }))
                    }

                    if (getNodeRunRequest.position === 1) {
                        throw new Error('not found')
                    }

                    return Promise.reject(new Error('Node position not present in wf Run'))
                }
            } as any))


            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor, LHClient.getInstance('ANY_TOKEN'))
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraphForWfRun(wfSpec, wfSpecName, WF_RUN_ID, 0, 'invalid-thread-spec')

            expect(wfSpecInReactFlowFormat.nodes[0].data.nodeHasRun).toEqual(true)
            expect(wfSpecInReactFlowFormat.nodes[1].data.nodeHasRun).toEqual(false)
        })

        it('should return a graph for the entrypoint thread spec if the provided one was not found', async () => {
            const WF_RUN_ID = '61e8c3cdba664d929c3c99d2c1a4e91a'

            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const wfRun: WfRun = {
                'id': {
                    'id': WF_RUN_ID
                },
                'wfSpecId': {
                    'name': 'example-interrupt-handler',
                    'majorVersion': 0 ,
                    'revision': 0
                },
                'oldWfSpecVersions': [],
                'greatestThreadrunNumber': 0,
                'status': LHStatus.COMPLETED,
                'startTime': '2023-10-24T16:56:36.161Z',
                'endTime': '2023-10-24T16:56:36.211Z',
                'threadRuns': [
                    {
                        'wfSpecId': {
                            'name': 'example-child-thread',
                            'majorVersion': 0 ,
                            'revision': 0
                        },
                        'number': 0,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': 'entrypoint',
                        'startTime': '2023-10-24T16:56:36.163Z',
                        'endTime': '2023-10-24T16:56:36.211Z',
                        'childThreadIds': [],
                        'haltReasons': [],
                        'currentNodePosition': 0,
                        'handledFailedChildren': [],
                        'type': ThreadType.ENTRYPOINT
                    }
                ],
                'pendingInterrupts': [],
                'pendingFailures': []
            }


            const spy = jest.spyOn(LHClient, 'getInstance')
            spy.mockImplementationOnce(() => (
                {
                    getWfRun: (_: any): Promise<WfRun> => {
                        return Promise.resolve(wfRun)
                    },
                    getNodeRun: (getNodeRunRequest: {
                        wfRunId: string,
                        threadNumber: number,
                        position: number
                    }): Promise<NodeRun> => {
                        if (getNodeRunRequest.position === 0) {
                            return Promise.resolve({
                                id: {
                                    wfRunId: {
                                        id: '56021fd8a2054563b25b595e6162b00c'
                                    },
                                    threadRunNumber: 1,
                                    position: 0
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-24T16:56:36.191Z',
                                'endTime': '2023-10-24T16:56:36.191Z',
                                'wfSpecId': {
                                    name: 'example-child-thread',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '0-entrypoint-ENTRYPOINT',
                                'failures': [],
                                'entrypoint': {},
                                'failureHandlerIds': []
                            } as NodeRun)
                        }

                        return Promise.reject(new Error('Node position not present in wf Run'))
                    }
                } as any))

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor, LHClient.getInstance('ANY_TOKEN'))
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraphForWfRun(wfSpec, wfSpecName, WF_RUN_ID, 0, 'invalid-thread-spec')

            expect(wfSpecInReactFlowFormat.nodes[0]).toEqual({
                id: '0-entrypoint-ENTRYPOINT',
                data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: true },
                position: { x: expect.any(Number), y: expect.any(Number) },
                positionInThreadRun: 0,
                type: 'entrypointNodeType',
                lhNode: LHNode.fromJSON({
                    'outgoingEdges': [],
                    'variableMutations': [],
                    'failureHandlers': [],
                    'entrypoint': {}
                })
            })
        })

        it('should return a graph for the entrypoint thread spec when thread spec was not provided at all', async () => {
            const WF_RUN_ID = '61e8c3cdba664d929c3c99d2c1a4e91a'

            const wfSpec: WfSpec = {
                'id': {
                    'name': 'evaluate-transaction',
                    'majorVersion': 1,
                    'revision': 0
                },
                'frozenVariables': [],
                'createdAt': '2023-10-12T15:40:16.573Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'entrypoint': {}
                            }
                        },
                        'variableDefs': [],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const wfRun: WfRun = {
                'id': {
                    'id': WF_RUN_ID
                },
                'wfSpecId': {
                    'name': 'example-child-threadr',
                    'majorVersion': 0 ,
                    'revision': 0
                },
                'oldWfSpecVersions': [],
                'greatestThreadrunNumber': 0,
                'status': LHStatus.COMPLETED,
                'startTime': '2023-10-24T16:56:36.161Z',
                'endTime': '2023-10-24T16:56:36.211Z',
                'threadRuns': [
                    {
                        'wfSpecId': {
                            'name': 'example-child-thread',
                            'majorVersion': 0,
                            'revision': 0
                        },
                        'number': 0,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': 'entrypoint',
                        'startTime': '2023-10-24T16:56:36.163Z',
                        'endTime': '2023-10-24T16:56:36.211Z',
                        'childThreadIds': [],
                        'haltReasons': [],
                        'currentNodePosition': 0,
                        'handledFailedChildren': [],
                        'type': ThreadType.ENTRYPOINT
                    }
                ],
                'pendingInterrupts': [],
                'pendingFailures': []
            }

            const spy = jest.spyOn(LHClient, 'getInstance')
            spy.mockImplementationOnce(() => (
                {
                    getWfRun: (_: any): Promise<WfRun> => {
                        return Promise.resolve(wfRun)
                    },
                    getNodeRun: (getNodeRunRequest: {
                        wfRunId: string,
                        threadNumber: number,
                        position: number
                    }): Promise<NodeRun> => {
                        if (getNodeRunRequest.position === 0) {
                            return Promise.resolve({
                                id: {
                                    wfRunId: {
                                        id: '56021fd8a2054563b25b595e6162b00c'
                                    },
                                    'threadRunNumber': 1,
                                    'position': 0,
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-24T16:56:36.191Z',
                                'endTime': '2023-10-24T16:56:36.191Z',
                                'wfSpecId': {
                                    name: 'example-child-thread',
                                    majorVersion: 0,
                                    revision: 0

                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '0-entrypoint-ENTRYPOINT',
                                'failures': [],
                                'entrypoint': {},
                                'failureHandlerIds': []
                            } as NodeRun
                            )
                        }

                        return Promise.reject(new Error('Node position not present in wf Run'))
                    }
                } as any))

            const mapper: GraphLayouter = new GraphLayouter(elk, labelExtractor, LHClient.getInstance('ANY_TOKEN'))
            const wfSpecInReactFlowFormat: ReactFlowGraph = await mapper.getLayoutedGraphForWfRun(wfSpec, wfSpecName, WF_RUN_ID, 0)

            expect(wfSpecInReactFlowFormat.nodes[0]).toEqual({
                id: '0-entrypoint-ENTRYPOINT',
                data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: true },
                position: { x: expect.any(Number), y: expect.any(Number) },
                type: 'entrypointNodeType',
                positionInThreadRun: 0,
                lhNode: LHNode.fromJSON({
                    'outgoingEdges': [],
                    'variableMutations': [],
                    'failureHandlers': [],
                    'entrypoint': {}
                })
            })
        })

        it('nodes that were already executed for the wfRun should be marked as so on a RUNNING task', async () => {
            const wfSpec: WfSpec = {
                id: {
                    'name': 'evaluate-ai-decision',
                    majorVersion: 0,
                    revision: 0
                },
                frozenVariables: [],
                'createdAt': '2023-10-23T19:50:53.488Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-evaluate-risk-of-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-nop-NOP',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'was-it-a-risky-decision',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {}
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'evaluate-risk-of-decision',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        }
                                    ]
                                }
                            },
                            '2-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-save-risk-evaluation-results-TASK',
                                        'condition': {
                                            'comparator': Comparator.EQUALS,
                                            'left': {
                                                'variableName': 'was-it-a-risky-decision'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'bool': false
                                                }
                                            }
                                        },
                                        'variableMutations': []
                                    },
                                    {
                                        'sinkNodeName': '5-explain-decision-TASK',
                                        'condition': {
                                            'comparator': Comparator.NOT_EQUALS,
                                            'left': {
                                                'variableName': 'was-it-a-risky-decision'
                                            },
                                            'right': {
                                                'literalValue': {
                                                    'bool': false
                                                }
                                            }
                                        },
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '3-save-risk-evaluation-results-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '4-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'save-risk-evaluation-results',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        },
                                        {
                                            'variableName': 'was-it-a-risky-decision'
                                        }
                                    ]
                                }
                            },
                            '4-nop-NOP': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '8-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'nop': {}
                            },
                            '5-explain-decision-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '6-risk-approval-form-USER_TASK',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'decision-explanation',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {}
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'explain-decision',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        }
                                    ]
                                }
                            },
                            '6-risk-approval-form-USER_TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '7-save-risk-evaluation-results-TASK',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'is-risk-approved',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {
                                                    'jsonpath': '$.isApproved'
                                                }
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'userTask': {
                                    'userTaskDefName': 'risk-approval-form',
                                    'userGroup': {
                                        'literalValue': {
                                            'str': 'risk-management'
                                        }
                                    },
                                    'actions': [],
                                    'userTaskDefVersion': 1
                                }
                            },
                            '7-save-risk-evaluation-results-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '4-nop-NOP',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'save-risk-evaluation-results',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'request-id'
                                        },
                                        {
                                            'variableName': 'is-risk-approved'
                                        }
                                    ]
                                }
                            },
                            '8-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.STR,
                                    'name': 'request-id',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.BOOL,
                                    'name': 'was-it-a-risky-decision',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.STR,
                                    'name': 'decision-explanation',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.BOOL,
                                    'name': 'is-risk-approved',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            }
                        ],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const CURRENT_NODE_POSITION = 4
            const WF_RUN_ID = '61e8c3cdba664d929c3c99d2c1a4e91a'

            const wfRun: WfRun = {
                'id': {
                    'id': WF_RUN_ID
                },
                'wfSpecId': {
                    'name': 'evaluate-ai-decision',
                    'majorVersion': 0,
                    'revision': 0
                },
                'greatestThreadrunNumber': 0,
                'oldWfSpecVersions': [],
                'status': LHStatus.COMPLETED,
                'startTime': '2023-10-23T19:55:42.243Z',
                'endTime': '2023-10-23T19:56:38.251Z',
                'threadRuns': [
                    {
                        'wfSpecId': {
                            'name': 'evaluate-ai-decision',
                            'majorVersion': 0,
                            'revision': 0
                        },
                        'number': 0,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': 'entrypoint',
                        'startTime': '2023-10-23T19:55:42.245Z',
                        'endTime': '2023-10-23T19:56:38.251Z',
                        'childThreadIds': [],
                        'haltReasons': [],
                        'currentNodePosition': CURRENT_NODE_POSITION,
                        'handledFailedChildren': [],
                        'type': ThreadType.ENTRYPOINT
                    }
                ],
                'pendingInterrupts': [],
                'pendingFailures': []
            }

            const expectedNodes: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    positionInThreadRun: 0,
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-evaluate-risk-of-decision-TASK',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'entrypoint': {}
                    } as LHNode
                },
                {
                    id: '1-evaluate-risk-of-decision-TASK',
                    data: { label: '1-evaluate-risk-of-decision-TASK', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    positionInThreadRun: 1,
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '2-nop-NOP',
                                'variableMutations': [
                                    {
                                        'lhsName': 'was-it-a-risky-decision',
                                        'operation': VariableMutationType.ASSIGN,
                                        nodeOutput: {
                                            jsonpath: undefined
                                        }
                                    }
                                ]
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name: 'evaluate-risk-of-decision'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                }
                            ]
                        }
                    } as LHNode
                },
                {
                    id: '2-nop-NOP',
                    data: { label: '2-nop-NOP', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    positionInThreadRun: 2,
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '3-save-risk-evaluation-results-TASK',
                                'condition': {
                                    'comparator': Comparator.EQUALS,
                                    'left': {
                                        'variableName': 'was-it-a-risky-decision'
                                    },
                                    'right': {
                                        'literalValue': {
                                            'bool': false
                                        }
                                    }
                                },
                                'variableMutations': []
                            },
                            {
                                'sinkNodeName': '5-explain-decision-TASK',
                                'condition': {
                                    'comparator': Comparator.NOT_EQUALS,
                                    'left': {
                                        'variableName': 'was-it-a-risky-decision'
                                    },
                                    'right': {
                                        'literalValue': {
                                            'bool': false
                                        }
                                    }
                                },
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'nop': {}
                    } as LHNode
                },
                {
                    id: '3-save-risk-evaluation-results-TASK',
                    data: { label: '3-save-risk-evaluation-results-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '4-nop-NOP',
                                'variableMutations': [],
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name:  'save-risk-evaluation-results'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                },
                                {
                                    'variableName': 'was-it-a-risky-decision'
                                }
                            ]
                        }
                    } as LHNode
                },
                {
                    id: '4-nop-NOP',
                    data: { label: '4-nop-NOP', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'nopNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '8-exit-EXIT',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'nop': {}
                    }
                },
                {
                    id: '5-explain-decision-TASK',
                    data: { label: '5-explain-decision-TASK', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    positionInThreadRun: 3,
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '6-risk-approval-form-USER_TASK',
                                'variableMutations': [
                                    {
                                        'lhsName': 'decision-explanation',
                                        'operation': VariableMutationType.ASSIGN,
                                        'nodeOutput': {}
                                    }
                                ]
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name:'explain-decision'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                }
                            ]
                        }
                    } as LHNode
                },
                {
                    id: '6-risk-approval-form-USER_TASK',
                    data: { label: '6-risk-approval-form-USER_TASK', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'userTaskNodeType',
                    positionInThreadRun: 4,
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '7-save-risk-evaluation-results-TASK',
                                'variableMutations': [
                                    {
                                        'lhsName': 'is-risk-approved',
                                        'operation': VariableMutationType.ASSIGN,
                                        'nodeOutput': {
                                            'jsonpath': '$.isApproved'
                                        }
                                    }
                                ]
                            }
                        ],
                        'failureHandlers': [],
                        'userTask': {
                            'userTaskDefName': 'risk-approval-form',
                            'userGroup': {
                                'literalValue': {
                                    'str': 'risk-management'
                                }
                            },
                            'actions': [],
                            'userTaskDefVersion': 1
                        }
                    } as LHNode
                },
                {
                    id: '7-save-risk-evaluation-results-TASK',
                    data: { label: '7-save-risk-evaluation-results-TASK', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    lhNode: {
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '4-nop-NOP',
                                'variableMutations': []
                            }
                        ],
                        'failureHandlers': [],
                        'task': {
                            taskDefId: {
                                name: 'save-risk-evaluation-results'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'request-id'
                                },
                                {
                                    'variableName': 'is-risk-approved'
                                }
                            ]
                        }
                    } as LHNode
                },
                {
                    id: '8-exit-EXIT',
                    data: { label: '8-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: {
                        'outgoingEdges': [],
                        'failureHandlers': [],
                        exit: {}
                    } as LHNode
                }
            ]


            const spy = jest.spyOn(LHClient, 'getInstance')
            spy.mockImplementationOnce(() => (
                {
                    getWfRun: (_: any): Promise<WfRun> => {
                        return Promise.resolve(wfRun)
                    },
                    getNodeRun: (getNodeRunRequest: {
                        wfRunId: string,
                        threadNumber: number,
                        position: number
                    }): Promise<NodeRun> => {
                        if (getNodeRunRequest.position === 0) {
                            return Promise.resolve({
                                id: {
                                    wfRunId: {
                                        id: WF_RUN_ID
                                    },
                                    threadRunNumber: 0,
                                    position: 0,
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-23T20:47:00.193Z',
                                'endTime': '2023-10-23T20:47:00.193Z',
                                'wfSpecId': {
                                    name: 'evaluate-ai-decision',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '0-entrypoint-ENTRYPOINT',
                                'failures': [],
                                'entrypoint': {},
                                'failureHandlerIds': []
                            } as NodeRun
                            )
                        }

                        if (getNodeRunRequest.position === 1) {
                            return Promise.resolve(NodeRun.fromJSON({
                                id: {
                                    wfRunId: {
                                        id: WF_RUN_ID
                                    },
                                    threadRunNumber: 0,
                                    position: 1,
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-23T20:47:00.193Z',
                                'endTime': '2023-10-23T20:47:00.198Z',
                                wfSpecId: {
                                    name: 'evaluate-ai-decision',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '1-evaluate-risk-of-decision-TASK',
                                'failures': [],
                                'task': {
                                    'taskRunId': {
                                        'wfRunId': '61e8c3cdba664d929c3c99d2c1a4e91a',
                                        'taskGuid': '78205aeaf201429eaccfdacb24c7f61c'
                                    }
                                },
                                'failureHandlerIds': []
                            })
                            )
                        }

                        if (getNodeRunRequest.position === 2) {
                            return Promise.resolve(NodeRun.fromJSON({
                                id: {
                                    wfRunId: {
                                        id: WF_RUN_ID
                                    },
                                    threadRunNumber: 0,
                                    position: 2,
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-23T20:47:00.199Z',
                                'endTime': '2023-10-23T20:47:00.199Z',
                                wfSpecId: {
                                    name: 'evaluate-ai-decision',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '2-nop-NOP',
                                'failures': [],
                                'entrypoint': {},
                                'failureHandlerIds': []
                            })
                            )
                        }

                        if (getNodeRunRequest.position === 3) {
                            return Promise.resolve(NodeRun.fromJSON({
                                id: {
                                    wfRunId: {
                                        id: WF_RUN_ID
                                    },
                                    threadRunNumber: 0,
                                    position: 3,
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-23T20:47:00.199Z',
                                'endTime': '2023-10-23T20:47:00.203Z',
                                wfSpecId: {
                                    name: 'evaluate-ai-decision',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '5-explain-decision-TASK',
                                'failures': [],
                                'task': {
                                    'taskRunId': {
                                        'wfRunId': '61e8c3cdba664d929c3c99d2c1a4e91a',
                                        'taskGuid': '1289cbce3eef4918b6b8e22da5f018e9'
                                    }
                                },
                                'failureHandlerIds': []
                            })
                            )
                        }

                        if (getNodeRunRequest.position === 4) {
                            return Promise.resolve(NodeRun.fromJSON({
                                id: {
                                    wfRunId: {
                                        id: WF_RUN_ID
                                    },
                                    threadRunNumber: 0,
                                    position: 4,
                                },
                                'status': 'RUNNING',
                                'arrivalTime': '2023-10-23T20:47:00.207Z',
                                wfSpecId: {
                                    name: 'evaluate-ai-decision',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': 'entrypoint',
                                'nodeName': '6-risk-approval-form-USER_TASK',
                                'failures': [],
                                'userTask': {
                                    'userTaskRunId': {
                                        'wfRunId': '61e8c3cdba664d929c3c99d2c1a4e91a',
                                        'userTaskGuid': 'cd2ec2ca428f4506bdc834707ddb7564'
                                    }
                                },
                                'failureHandlerIds': []
                            })
                            )
                        }

                        return Promise.reject(new Error('Node position not present in wf Run'))
                    }
                } as any))

            const graphLayouter: GraphLayouter = new GraphLayouter(new ElkConstructor(), labelExtractor, LHClient.getInstance('ANY_TOKEN'))
            const layoutedGraphForWfRun: ReactFlowGraph = await graphLayouter.getLayoutedGraphForWfRun(wfSpec, wfSpecName, WF_RUN_ID, 0)

            expect(layoutedGraphForWfRun.nodes).toEqual(expectedNodes)
        })

        it('should mark the nodes that has run for the desired Thread Spec + Thread Run Number combination', async () => {
            const threadSpec = 'spawned-thread'
            const wfSpec: WfSpec = {
                id: {
                    name: 'example-child-thread',
                    majorVersion: 0,
                    revision: 0
                },
                frozenVariables: [],
                'createdAt': '2023-10-24T16:54:56.181Z',
                'status':  MetadataStatus.ACTIVE,
                'threadSpecs': {
                    'entrypoint': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-parent-task-1-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-parent-task-1-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-spawned-thread-START_THREAD',
                                        'variableMutations': [
                                            {
                                                'lhsName': 'parent-var',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {}
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'parent-task-1',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'parent-var'
                                        }
                                    ]
                                }
                            },
                            '2-spawned-thread-START_THREAD': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '3-threads-WAIT_FOR_THREADS',
                                        'variableMutations': [
                                            {
                                                'lhsName': '2-spawned-thread-START_THREAD',
                                                'operation': VariableMutationType.ASSIGN,
                                                'nodeOutput': {}
                                            }
                                        ]
                                    }
                                ],
                                'failureHandlers': [],
                                'startThread': {
                                    'threadSpecName': threadSpec,
                                    'variables': {
                                        'child-var': {
                                            'variableName': 'parent-var'
                                        }
                                    }
                                }
                            },
                            '3-threads-WAIT_FOR_THREADS': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '4-parent-task-2-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'waitForThreads': {
                                    'threads': [
                                        {
                                            'threadRunNumber': {
                                                'variableName': '2-spawned-thread-START_THREAD'
                                            }
                                        }
                                    ],
                                    'policy': WaitForThreadsPolicy.STOP_ON_FAILURE
                                }
                            },
                            '4-parent-task-2-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '5-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'parent-task-2',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': []
                                }
                            },
                            '5-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                exit: {
                                    failureDef: undefined
                                }
                            }
                        },
                        'variableDefs': [
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.INT,
                                    'name': 'parent-var',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.INT,
                                    'name': '2-spawned-thread-START_THREAD',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            }
                        ],
                        'interruptDefs': []
                    },
                    'spawned-thread': {
                        'nodes': {
                            '0-entrypoint-ENTRYPOINT': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '1-child-task-TASK',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'entrypoint': {}
                            },
                            '1-child-task-TASK': {
                                'outgoingEdges': [
                                    {
                                        'sinkNodeName': '2-exit-EXIT',
                                        'variableMutations': []
                                    }
                                ],
                                'failureHandlers': [],
                                'task': {
                                    'taskDefId': {
                                        'name': 'child-task',
                                    },
                                    'timeoutSeconds': 15,
                                    'retries': 0,
                                    'variables': [
                                        {
                                            'variableName': 'child-var'
                                        }
                                    ]
                                }
                            },
                            '2-exit-EXIT': {
                                'outgoingEdges': [],
                                'failureHandlers': [],
                                'exit': {}
                            }
                        },
                        'variableDefs': [
                            {
                                searchable: true,
                                required: true,
                                'varDef': {
                                    'type': VariableType.INT,
                                    'name': 'child-var',
                                    'defaultValue': undefined,
                                },
                                accessLevel: WfRunVariableAccessLevel.PUBLIC_VAR,
                                'jsonIndexes': [],
                            },
                        ],
                        'interruptDefs': []
                    }
                },
                'entrypointThreadName': 'entrypoint',

            }

            const CURRENT_NODE_POSITION = 1
            const WF_RUN_ID = '56021fd8a2054563b25b595e6162b00c'

            const threadRunNumber = 2
            const wfRun: WfRun = {
                'id': {
                    'id': '56021fd8a2054563b25b595e6162b00c'
                },
                'oldWfSpecVersions': [],
                'greatestThreadrunNumber': 2,
                'wfSpecId': {
                    'name': 'example-child-thread',
                    'majorVersion': 0,
                    'revision': 0,
                },
                'status': LHStatus.COMPLETED,
                'startTime': '2023-10-24T16:56:36.161Z',
                'endTime': '2023-10-24T16:56:36.211Z',
                'threadRuns': [
                    {
                        'wfSpecId': {
                            'name': 'example-child-thread',
                            'majorVersion': 0,
                            'revision': 0,
                        },
                        'number': 0,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': 'entrypoint',
                        'startTime': '2023-10-24T16:56:36.163Z',
                        'endTime': '2023-10-24T16:56:36.211Z',
                        'childThreadIds': [
                            1
                        ],
                        'haltReasons': [],
                        'currentNodePosition': 5,
                        'handledFailedChildren': [],
                        'type': ThreadType.ENTRYPOINT
                    },
                    {
                        'wfSpecId': {
                            'name': 'example-child-thread',
                            'majorVersion': 0,
                            'revision': 0,
                        },
                        'number': 1,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': threadSpec,
                        'startTime': '2023-10-24T16:56:36.191Z',
                        'endTime': '2023-10-24T16:56:36.203Z',
                        'childThreadIds': [],
                        'parentThreadId': 0,
                        'haltReasons': [],
                        'currentNodePosition': 0,
                        'handledFailedChildren': [],
                        'type': ThreadType.CHILD
                    },
                    {
                        'wfSpecId': {
                            'name': 'example-child-thread',
                            'majorVersion': 0,
                            'revision': 0,
                        },
                        'number': threadRunNumber,
                        'status': LHStatus.COMPLETED,
                        'threadSpecName': threadSpec,
                        'startTime': '2023-10-24T16:56:36.191Z',
                        'endTime': '2023-10-24T16:56:36.203Z',
                        'childThreadIds': [],
                        'parentThreadId': 0,
                        'haltReasons': [],
                        'currentNodePosition': CURRENT_NODE_POSITION,
                        'handledFailedChildren': [],
                        'type': ThreadType.CHILD
                    }
                ],
                'pendingInterrupts': [],
                'pendingFailures': []
            }

            const expectedNodesForSpawnThreadRun2: ReactFlowNodeWithLHInfo[] = [
                {
                    id: '0-entrypoint-ENTRYPOINT',
                    data: { label: '0-entrypoint-ENTRYPOINT', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'entrypointNodeType',
                    positionInThreadRun: 0,
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '1-child-task-TASK'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'entrypoint': {}
                    })
                },
                {
                    id: '1-child-task-TASK',
                    data: { label: '1-child-task-TASK', failureHandlers: [], nodeHasRun: true },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'taskNodeType',
                    positionInThreadRun: 1,
                    lhNode: LHNode.fromJSON({
                        'outgoingEdges': [
                            {
                                'sinkNodeName': '2-exit-EXIT'
                            }
                        ],
                        'variableMutations': [],
                        'failureHandlers': [],
                        'task': {
                            'taskDefId': {
                                name: 'child-task'
                            },
                            'timeoutSeconds': 15,
                            'retries': 0,
                            'variables': [
                                {
                                    'variableName': 'child-var'
                                }
                            ]
                        }
                    })
                },
                {
                    id: '2-exit-EXIT',
                    data: { label: '2-exit-EXIT', failureHandlers: [], nodeHasRun: false },
                    position: { x: expect.any(Number), y: expect.any(Number) },
                    type: 'exitNodeType',
                    lhNode: {
                        outgoingEdges: [],
                        failureHandlers: [],
                        exit: {}
                    }
                },
            ]


            const spy = jest.spyOn(LHClient, 'getInstance')
            spy.mockImplementationOnce(() => (
                {
                    getWfRun: (_: any): Promise<WfRun> => {
                        return Promise.resolve(wfRun)
                    },
                    getNodeRun: (getNodeRunRequest: {
                        wfRunId: string,
                        threadNumber: number,
                        position: number
                    }): Promise<NodeRun> => {
                        if (getNodeRunRequest.position === 0) {
                            return Promise.resolve({
                                id: {
                                    wfRunId: {
                                        id: '56021fd8a2054563b25b595e6162b00c'
                                    },
                                    position: 0,
                                    threadRunNumber: 1
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-24T16:56:36.191Z',
                                'endTime': '2023-10-24T16:56:36.191Z',
                                'wfSpecId': {
                                    name: 'example-child-thread',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': threadSpec,
                                'nodeName': '0-entrypoint-ENTRYPOINT',
                                'failures': [],
                                'entrypoint': {},
                                'failureHandlerIds': []
                            } as NodeRun
                            )
                        }

                        if (getNodeRunRequest.position === 1) {
                            return Promise.resolve({
                                id: {
                                    wfRunId: {
                                        id: '56021fd8a2054563b25b595e6162b00c'
                                    },
                                    position: 1,
                                    threadRunNumber: 1
                                },
                                'status': 'COMPLETED',
                                'arrivalTime': '2023-10-24T16:56:36.191Z',
                                'endTime': '2023-10-24T16:56:36.200Z',
                                wfSpecId: {
                                    name: 'example-child-thread',
                                    majorVersion: 0,
                                    revision: 0
                                },
                                'threadSpecName': threadSpec,
                                'nodeName': '1-child-task-TASK',
                                'failures': [],
                                'task': {
                                    'taskRunId': {
                                        'wfRunId': {
                                            id: '56021fd8a2054563b25b595e6162b00c'
                                        },
                                        'taskGuid': 'a86ea08caf4c40bfaf3c2f93ac404285'
                                    }
                                },
                                'failureHandlerIds': []
                            } as NodeRun)
                        }

                        return Promise.reject(new Error('Node position not present in wf Run'))
                    }
                } as any))

            const graphLayouter: GraphLayouter = new GraphLayouter(new ElkConstructor(), labelExtractor, LHClient.getInstance('ANY_TOKEN'))
            const layoutedGraphForSpawnThreadsRun2: ReactFlowGraph = await graphLayouter.getLayoutedGraphForWfRun(wfSpec, wfSpecName, WF_RUN_ID, threadRunNumber, threadSpec)

            expect(layoutedGraphForSpawnThreadsRun2.nodes).toEqual(expectedNodesForSpawnThreadRun2)
        })
    })
})
