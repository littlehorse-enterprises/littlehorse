import { ThreadRunWithNodeRuns } from '@/app/actions/getWfRun'
import dagre from '@dagrejs/dagre'
import { WfRun } from 'littlehorse-client/proto'
import { FC, useCallback, useEffect } from 'react'
import { Edge, Node, useReactFlow, useStore } from '@xyflow/react'

// used to calculate the width of the
export const EDGE_WIDTH = 200

const threadSpecs = {
  "entrypoint": {
    "nodes": {
      "0-entrypoint-ENTRYPOINT": {
        "outgoingEdges": [{
          "sinkNodeName": "1-task-a-TASK",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "entrypoint": {}
      },
      "1-task-a-TASK": {
        "outgoingEdges": [{
          "sinkNodeName": "2-nop-NOP",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "task": {
          "taskDefId": {
            "name": "task-a"
          },
          "timeoutSeconds": 0,
          "retries": 0,
          "variables": []
        }
      },
      "2-nop-NOP": {
        "outgoingEdges": [{
          "sinkNodeName": "3-task-b-TASK",
          "condition": {
            "comparator": "LESS_THAN",
            "left": {
              "jsonPath": "$.bar",
              "variableName": "foo"
            },
            "right": {
              "literalValue": {
                "int": "10"
              }
            }
          },
          "variableMutations": []
        }, {
          "sinkNodeName": "4-task-c-TASK",
          "condition": {
            "comparator": "LESS_THAN_EQ",
            "left": {
              "jsonPath": "$.bar",
              "variableName": "foo"
            },
            "right": {
              "literalValue": {
                "int": "10"
              }
            }
          },
          "variableMutations": []
        }, {
          "sinkNodeName": "99-task-omega-TASK",
          "condition": {
            "comparator": "GREATER_THAN",
            "left": {
              "jsonPath": "$.bar",
              "variableName": "foo"
            },
            "right": {
              "literalValue": {
                "int": "99"
              }
            }
          },
          "variableMutations": []
        }],
        "failureHandlers": [],
        "nop": {}
      },
      "3-task-b-TASK": {
        "outgoingEdges": [{
          "sinkNodeName": "5-nop-NOP",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "task": {
          "taskDefId": {
            "name": "task-b"
          },
          "timeoutSeconds": 0,
          "retries": 0,
          "variables": []
        }
      },
      "99-task-omega-TASK": {
        "outgoingEdges": [{
          "sinkNodeName": "5-nop-NOP",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "task": {
          "taskDefId": {
            "name": "task-omega"
          },
          "timeoutSeconds": 0,
          "retries": 0,
          "variables": []
        }
      },
      "4-task-c-TASK": {
        "outgoingEdges": [{
          "sinkNodeName": "5-nop-NOP",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "task": {
          "taskDefId": {
            "name": "task-c"
          },
          "timeoutSeconds": 0,
          "retries": 0,
          "variables": []
        }
      },
      "5-nop-NOP": {
        "outgoingEdges": [{
          "sinkNodeName": "6-task-d-TASK",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "nop": {}
      },
      "6-task-d-TASK": {
        "outgoingEdges": [{
          "sinkNodeName": "7-exit-EXIT",
          "variableMutations": []
        }],
        "failureHandlers": [],
        "task": {
          "taskDefId": {
            "name": "task-d"
          },
          "timeoutSeconds": 0,
          "retries": 0,
          "variables": []
        }
      },
      "7-exit-EXIT": {
        "outgoingEdges": [],
        "failureHandlers": [],
        "exit": {}
      }
    },
    "variableDefs": [{
      "varDef": {
        "type": "JSON_OBJ",
        "name": "foo",
        "maskedValue": false
      },
      "required": false,
      "searchable": false,
      "jsonIndexes": [],
      "accessLevel": "PRIVATE_VAR"
    }],
    "interruptDefs": []
  }
}

export const Layouter: FC<{ wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }; nodeRunNameToBeHighlighted?: string }> = ({
  wfRun,
  nodeRunNameToBeHighlighted,
}) => {
  const nodes = useStore(store => store.nodes)
  const edges = useStore(store => store.edges)
  const setNodes = useStore(store => store.setNodes)
  const { fitView } = useReactFlow()

  const onLoad = useCallback(
    (nodes: Node[], edges: Edge[]) => {
      const dagreGraph = new dagre.graphlib.Graph()
      dagreGraph.setDefaultEdgeLabel(() => ({}))
      dagreGraph.setGraph({ rankdir: 'LR', align: 'UL', ranksep: 100 })
      nodes.forEach(node => {
        dagreGraph.setNode(node.id, { width: node.width, height: node.height })
      })

      edges.forEach(edge => {
        dagreGraph.setEdge(edge.source, edge.target, { width: edge.label ? EDGE_WIDTH : undefined })
      })

      dagre.layout(dagreGraph)

      const layoutedNodes = nodes.map(node => {
        const nodeWithPosition = dagreGraph.node(node.id)

        const [nodeName, threadRunName] = node.id.split(':')
        const threadRun = wfRun?.threadRuns.find(threadRun => threadRun.threadSpecName === threadRunName) as ThreadRunWithNodeRuns | undefined

        const nodeRun = threadRun?.nodeRuns.find(nodeRun => {
          return nodeRun.nodeName === nodeName
        })
        const nodeRunsList = threadRun?.nodeRuns.filter(nodeRun => {
          return nodeRun.nodeName === nodeName
        })

        const fade = threadRun?.nodeRuns && !nodeRun || (wfRun && !threadRun)
        const nodeNeedsToBeHighlighted = nodeName === nodeRunNameToBeHighlighted

        return {
          ...node,
          data: { ...node.data, nodeRun, fade, nodeNeedsToBeHighlighted, nodeRunsList },
          position: { x: nodeWithPosition.x - node.width! / 2, y: nodeWithPosition.y - node.height! / 2 },
          layouted: true,
        }
      })

      setNodes(layoutedNodes)
      fitView()
    },
    [fitView, setNodes, nodeRunNameToBeHighlighted]
  )

  useEffect(() => {
    console.log(nodes, edges)
    if (
      nodes.some(
        (node: Node & { layouted?: boolean }) => node.width !== undefined && node.height !== undefined && !node.layouted
      )
    ) {
      onLoad(nodes, edges)
    }
  }, [nodes, edges, onLoad])
  return <></>
}
