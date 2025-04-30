import dagre from '@dagrejs/dagre';
import {
  Background,
  ConnectionLineType,
  Controls,
  Edge, Node,
  OnEdgesChange,
  OnNodesChange,
  Position,
  ReactFlow,
  useEdgesState,
  useNodesState
} from '@xyflow/react';
import { useCallback } from 'react';

import '@xyflow/react/dist/style.css';
import { edgeTypes } from './EdgeTypes';
import nodeTypes from './NodeTypes';

const dagreGraph = new dagre.graphlib.Graph().setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;

const getLayoutedElements = (nodes: Node[], edges: Edge[]) => {
    dagreGraph.setGraph({ rankdir: 'LR' });

    nodes.forEach((node) => {
        dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
    });

    edges.forEach((edge) => {
        dagreGraph.setEdge(edge.source, edge.target);
    });

    dagre.layout(dagreGraph);

    const newNodes = nodes.map((node) => {
        const nodeWithPosition = dagreGraph.node(node.id);
        const newNode: Node = {
            ...node,
            targetPosition: Position.Left,
            sourcePosition: Position.Right,
            // We are shifting the dagre node position (anchor=center center) to the top left
            // so it matches the React Flow node anchor point (top left).
            position: {
                x: nodeWithPosition.x - nodeWidth / 2,
                y: nodeWithPosition.y - nodeHeight / 2,
            },
        };

        return newNode;
    });

    return { nodes: newNodes, edges };
};

const position = { x: 0, y: 0 };
const edgeType = 'smoothstep';

export const initialNodes: Node[] = [
    {
        id: '1',
        type: 'ENTRYPOINT',
        data: { label: 'input' },
        position,
    },
    {
        id: '2',
        type: 'TASK',
        data: {
            // "outgoingEdges": [
            //     {
            //         "sinkNodeName": "5-nop-NOP",
            //         "variableMutations": [
            //             {
            //                 "lhsName": "email-request",
            //                 "operation": "ASSIGN",
            //                 "rhsAssignment": {
            //                     "nodeOutput": {
            //                         "nodeName": "4-TaskDefNames.GENERAL_QUESTION-TASK"
            //                     }
            //                 }
            //             }
            //         ]
            //     }
            // ],
            "failureHandlers": [],
            "task": {
                "taskDefId": {
                    "name": "general-question"
                },
                "timeoutSeconds": 60,
                "retries": 3,
                "variables": [
                    {
                        "variableName": "customer-message"
                    },
                    {
                        "variableName": "customer-data"
                    }
                ]
            },
            "taskDefId": {
                "name": "general-question"
            },
            "timeoutSeconds": 60,
            "retries": 3,
            "variables": [
                {
                    "variableName": "customer-message"
                },
                {
                    "variableName": "customer-data"
                }
            ]
        },
        position,
    },
    {
        id: '3',
        type: 'NOP',
        data: {
            "outgoingEdges": [
                {
                    "sinkNodeName": "5-nop-NOP",
                    "variableMutations": [
                        {
                            "lhsName": "email-request",
                            "operation": "ASSIGN",
                            "rhsAssignment": {
                                "nodeOutput": {
                                    "nodeName": "4-TaskDefNames.GENERAL_QUESTION-TASK"
                                }
                            }
                        }
                    ]
                }
            ],
        },
        position,
    },
    {
        id: '4a',
        type: 'TASK',
        data: {
            "outgoingEdges": [
                {
                    "sinkNodeName": "5-nop-NOP",
                    "variableMutations": [
                        {
                            "lhsName": "email-request",
                            "operation": "ASSIGN",
                            "rhsAssignment": {
                                "nodeOutput": {
                                    "nodeName": "4-TaskDefNames.GENERAL_QUESTION-TASK"
                                }
                            }
                        }
                    ]
                }
            ],
            "failureHandlers": [],
            "task": {
                "taskDefId": {
                    "name": "first-if"
                },
                "timeoutSeconds": 60,
                "retries": 3,
                "variables": [
                    {
                        "variableName": "customer-message"
                    },
                    {
                        "variableName": "customer-data"
                    }
                ]
            },
            "taskDefId": {
                "name": "general-question"
            },
            "timeoutSeconds": 60,
            "retries": 3,
            "variables": [
                {
                    "variableName": "customer-message"
                },
                {
                    "variableName": "customer-data"
                }
            ]
        },
        position,
    },
    {
        id: '4b',
        type: 'TASK',
        data: {
            "outgoingEdges": [
                {
                    "sinkNodeName": "5-nop-NOP",
                    "variableMutations": [
                        {
                            "lhsName": "email-request",
                            "operation": "ASSIGN",
                            "rhsAssignment": {
                                "nodeOutput": {
                                    "nodeName": "4-TaskDefNames.GENERAL_QUESTION-TASK"
                                }
                            }
                        }
                    ]
                }
            ],
            "failureHandlers": [],
            "task": {
                "taskDefId": {
                    "name": "second-if"
                },
                "timeoutSeconds": 60,
                "retries": 3,
                "variables": [
                    {
                        "variableName": "customer-message"
                    },
                    {
                        "variableName": "customer-data"
                    }
                ]
            },
            "taskDefId": {
                "name": "general-question"
            },
            "timeoutSeconds": 60,
            "retries": 3,
            "variables": [
                {
                    "variableName": "customer-message"
                },
                {
                    "variableName": "customer-data"
                }
            ]
        },
        position,
    },
    {
        id: '4c',
        type: 'TASK',
        data: {
            "outgoingEdges": [
                {
                    "sinkNodeName": "5-nop-NOP",
                    "variableMutations": [
                        {
                            "lhsName": "email-request",
                            "operation": "ASSIGN",
                            "rhsAssignment": {
                                "nodeOutput": {
                                    "nodeName": "4-TaskDefNames.GENERAL_QUESTION-TASK"
                                }
                            }
                        }
                    ]
                }
            ],
            "failureHandlers": [],
            "task": {
                "taskDefId": {
                    "name": "third-if"
                },
                "timeoutSeconds": 60,
                "retries": 3,
                "variables": [
                    {
                        "variableName": "customer-message"
                    },
                    {
                        "variableName": "customer-data"
                    }
                ]
            },
            "taskDefId": {
                "name": "general-question"
            },
            "timeoutSeconds": 60,
            "retries": 3,
            "variables": [
                {
                    "variableName": "customer-message"
                },
                {
                    "variableName": "customer-data"
                }
            ]
        },
        position,
    },
    {
        id: '5',
        type: 'NOP',
        data: {},
        position,
    },
    {
        id: '6',
        type: "EXIT",
        data: {},
        position,
    }
];
export const initialEdges = [
  {id: 'e12', source: '1', target: '2', type: edgeType, animated: true},
  {id: 'e23', source: '2', target: '3', type: edgeType, animated: true},
  {id: 'e34a', source: '3', target: '4a', type: edgeType, animated: true},
  {id: 'e34b', source: '3', target: '4b', type: edgeType, animated: true},
  {id: 'e34c', source: '3', target: '4c', type: edgeType, animated: true},
  {id: 'e4a5', source: '4a', target: '5', type: edgeType, animated: true},
  {id: 'e4b5', source: '4b', target: '5', type: edgeType, animated: true},
  {id: 'e4c5', source: '4c', target: '5', type: edgeType, animated: true},
  {id: 'e56', source: '5', target: '6', type: edgeType, animated: true},
];

export function Layouter({initialNodes, initialEdges}: {initialNodes: Node[], initialEdges: Edge[]}) {
    const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
        initialNodes,
        initialEdges,
    );
    const [nodes, setNodes, onNodesChange] = useNodesState(layoutedNodes);
    const [edges, setEdges, onEdgesChange] = useEdgesState(layoutedEdges);

    const onLayout = useCallback(
        () => {
            const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
                nodes,
                edges,
            );

            setNodes([...layoutedNodes]);
            setEdges([...layoutedEdges]);
        },
        [nodes, edges],
    );

    return (
        <ReactFlow
            nodes={nodes}
            edges={edges}
            className="min-h-[800px] min-w-full"
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            onNodesChange={onNodesChange as OnNodesChange<Node>}
            onEdgesChange={onEdgesChange as OnEdgesChange<Edge>}
            connectionLineType={ConnectionLineType.SmoothStep}
            fitView
            style={{ backgroundColor: '#F7F9FB' }}
        >
            <Background />
            <Controls/>
        </ReactFlow>
    );
}