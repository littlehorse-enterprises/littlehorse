// ! wip still

import { WfRunDetails } from '@/types/wfRunDetails'
import { type Node, type Edge, MarkerType } from '@xyflow/react'
import { NodeRun, WfSpec, Edge as LHEdge, ThreadSpec } from 'littlehorse-client/proto'

export function extractNodeData(wfSpec: WfSpec, wfRunDetails?: WfRunDetails): Node[] {
  const allNodeNames = new Set<string>()

  Object.values(wfSpec.threadSpecs).forEach((threadSpec: ThreadSpec) => {
    Object.keys(threadSpec.nodes).forEach(nodeName => {
      allNodeNames.add(nodeName)
    })
  })

  if (wfSpec && wfSpec.threadSpecs && wfSpec.entrypointThreadName) {
    const threadSpec = wfSpec.threadSpecs[wfSpec.entrypointThreadName]
    if (threadSpec && threadSpec.nodes) {
      Object.keys(threadSpec.nodes).forEach(nodeName => {
        allNodeNames.add(nodeName)
      })
    }
  }

  const nodes = Array.from(allNodeNames).map(nodeName => {
    const nodeRun = wfRunDetails?.nodeRuns?.find(nr => nr.nodeName === nodeName)

    let status = 'pending'
    if (nodeRun) {
      if (nodeRun.endTime) {
        status = 'completed'
      } else if (nodeRun.arrivalTime) {
        status = 'running'
      }
    }

    const data = {
      label: nodeName,
      status: status,
      type: nodeName.split('-').at(-1),
      ...nodeRun,
    }

    return {
      id: nodeName,
      data: data,
      type: 'task',
      position: { x: 0, y: 0 },
    } satisfies Node
  })

  return nodes
}

export function extractEdgeData(wfSpec: WfSpec): Edge[] {
  if (!wfSpec || !wfSpec.threadSpecs || !wfSpec.entrypointThreadName) {
    console.warn('Cannot extract edges - missing thread specs')
    return []
  }

  const threadSpec = wfSpec.threadSpecs[wfSpec.entrypointThreadName]
  if (!threadSpec || !threadSpec.nodes) {
    console.warn('Cannot extract edges - missing nodes in thread spec')
    return []
  }

  const edges = Object.entries(threadSpec.nodes).flatMap(([nodeName, nodeData]) => {
    if (!nodeData.outgoingEdges) return []

    return nodeData.outgoingEdges.map((edge: LHEdge) => {
      return {
        id: `${nodeName}->${edge.sinkNodeName}`,
        source: nodeName,
        target: edge.sinkNodeName,
        animated: true,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
      } satisfies Edge
    })
  })

  return edges
}

export function getNodeStatus(nodeRun: NodeRun | undefined): 'pending' | 'running' | 'completed' | 'failed' {
  if (!nodeRun) return 'pending'

  if (nodeRun.status) {
    const lhStatus = nodeRun.status.toLowerCase()
    if (lhStatus.includes('completed') || lhStatus.includes('success')) return 'completed'
    if (lhStatus.includes('running') || lhStatus.includes('active')) return 'running'
    if (lhStatus.includes('failed') || lhStatus.includes('error')) return 'failed'
  }

  if (nodeRun.endTime) return 'completed'
  if (nodeRun.arrivalTime) return 'running'

  return 'pending'
}
