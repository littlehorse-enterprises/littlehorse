"use client"

import type React from "react"

import { useCallback, useEffect } from "react"
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  type NodeTypes,
  useNodesState,
  useEdgesState,
  addEdge,
  type Connection,
  MarkerType,
  BackgroundVariant,
  Node,
  Edge,
  ConnectionLineType,
  Position,
} from "reactflow"
import dagre from "@dagrejs/dagre"
import "reactflow/dist/style.css"
import TaskNode from "@/components/flow/task-node"
import StartNode from "@/components/flow/start-node"
import EndNode from "@/components/flow/end-node"
import DecisionNode from "@/components/flow/decision-node"
import { useWorkflow } from "../context/workflow-context"
import { WfRunResponse } from "@/actions/getWfRun"
import { Edge as LHEdge, NodeRun } from "littlehorse-client/proto"

// Define node dimensions
const nodeWidth = 172;
const nodeHeight = 36;

// Layout function that positions nodes using dagre - always horizontal (LR)
const getLayoutedElements = (nodes: Node[], edges: Edge[]) => {
  console.log("Input to layout function - nodes:", nodes);
  console.log("Input to layout function - edges:", edges);

  if (!nodes.length) return { nodes, edges };

  // Create a new dagre graph instance each time to avoid issues with stale data
  const dagreGraph = new dagre.graphlib.Graph().setDefaultEdgeLabel(() => ({}));

  // Set direction to LR (Left to Right) for horizontal layout
  const direction = 'LR';
  dagreGraph.setGraph({ rankdir: direction });

  // Set nodes in the dagre graph
  nodes.forEach((node) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  // Set edges in the dagre graph
  edges.forEach((edge) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  // Run the dagre layout algorithm
  dagre.layout(dagreGraph);

  // Get node positions from dagre
  const layoutedNodes = nodes.map((node) => {
    const nodeWithPosition = dagreGraph.node(node.id);

    // Skip nodes that don't exist in the dagre graph
    if (!nodeWithPosition) {
      console.warn(`Node ${node.id} not found in dagre graph, using original position`);
      return node;
    }

    // Set node positions and source/target positions for horizontal layout
    return {
      ...node,
      targetPosition: Position.Left,
      sourcePosition: Position.Right,
      position: {
        x: nodeWithPosition.x - nodeWidth / 2,
        y: nodeWithPosition.y - nodeHeight / 2,
      },
    };
  });

  console.log("Output from layout function - nodes:", layoutedNodes);
  return { nodes: layoutedNodes, edges };
};

function extractNodeData(wfRun: WfRunResponse): Node[] {
  console.log("Extracting node data from wfRun:", wfRun);

  // Get all node names from the workflow specification
  const allNodeNames = new Set<string>();

  // First add nodes from nodeRuns
  wfRun.nodeRuns.forEach(nodeRun => {
    allNodeNames.add(nodeRun.nodeName);
  });

  // Then add nodes from the spec that might not have runs yet
  if (wfRun.wfSpec && wfRun.wfSpec.threadSpecs && wfRun.wfSpec.entrypointThreadName) {
    const threadSpec = wfRun.wfSpec.threadSpecs[wfRun.wfSpec.entrypointThreadName];
    if (threadSpec && threadSpec.nodes) {
      Object.keys(threadSpec.nodes).forEach(nodeName => {
        allNodeNames.add(nodeName);
      });
    }
  }

  console.log("All node names found:", Array.from(allNodeNames));

  // Create nodes for all node names
  const nodes = Array.from(allNodeNames).map(nodeName => {
    // Find the matching nodeRun if it exists
    const nodeRun = wfRun.nodeRuns.find(nr => nr.nodeName === nodeName);

    // Use the nodeRun data if available, otherwise create minimal node data
    const data = nodeRun || { nodeName };

    return {
      id: nodeName,
      data: data,
      type: "task", // Default type, could be refined based on node characteristics
      position: { x: 0, y: 0 },
    } satisfies Node;
  });

  console.log("Extracted nodes:", nodes);
  return nodes;
}

function extractEdgeData(wfRun: WfRunResponse): Edge[] {
  if (!wfRun.wfSpec || !wfRun.wfSpec.threadSpecs || !wfRun.wfSpec.entrypointThreadName) {
    console.warn("Cannot extract edges - missing thread specs");
    return [];
  }

  const threadSpec = wfRun.wfSpec.threadSpecs[wfRun.wfSpec.entrypointThreadName];
  if (!threadSpec || !threadSpec.nodes) {
    console.warn("Cannot extract edges - missing nodes in thread spec");
    return [];
  }

  const edges = Object.entries(threadSpec.nodes).flatMap(([nodeName, nodeData]) => {
    if (!nodeData.outgoingEdges) return [];

    return nodeData.outgoingEdges.map((edge: LHEdge) => {
      return {
        id: `${nodeName}->${edge.sinkNodeName}`,
        source: nodeName,
        target: edge.sinkNodeName,
        animated: true,
        markerEnd: {
          type: MarkerType.ArrowClosed,
        },
      }
    })
  });

  console.log("Extracted edges:", edges);
  return edges;
}

interface MainContentProps {
  isLeftSidebarExpanded?: boolean
  isRightSidebarExpanded?: boolean
  onNodeSelect?: (nodeId: string) => void
  selectedNodeId?: string
}

// Define custom node types
const nodeTypes: NodeTypes = {
  task: TaskNode,
  start: StartNode,
  end: EndNode,
  decision: DecisionNode,
}

export default function MainContent({
  isLeftSidebarExpanded,
  isRightSidebarExpanded,
  onNodeSelect,
  selectedNodeId,
}: MainContentProps) {
  const { wfSpec, wfRun, isLoading } = useWorkflow()
  console.log("wfRun", wfRun)

  // Add a null check before extracting node data
  const extractedNodes = wfRun ? extractNodeData(wfRun) : [];
  const extractedEdges = wfRun ? extractEdgeData(wfRun) : [];
  console.log("extractedNodes", extractedNodes)
  console.log("extractedEdges", extractedEdges)

  // Apply horizontal layout to the extracted nodes and edges
  const { nodes: layoutedNodes, edges: layoutedEdges } = getLayoutedElements(
    extractedNodes,
    extractedEdges
  );

  console.log("layoutedNodes", layoutedNodes)
  console.log("layoutedEdges", layoutedEdges)

  const [nodes, setNodes, onNodesChange] = useNodesState(layoutedNodes)
  const [edges, setEdges, onEdgesChange] = useEdgesState(layoutedEdges)

  // Update nodes and edges when data changes
  useEffect(() => {
    if (wfRun) {
      const newExtractedNodes = extractNodeData(wfRun);
      const newExtractedEdges = extractEdgeData(wfRun);

      console.log("Effect - newExtractedNodes:", newExtractedNodes.length);

      const { nodes: newLayoutedNodes, edges: newLayoutedEdges } = getLayoutedElements(
        newExtractedNodes,
        newExtractedEdges
      );

      console.log("Effect - newLayoutedNodes:", newLayoutedNodes.length);

      setNodes(newLayoutedNodes);
      setEdges(newLayoutedEdges);
    }
  }, [wfRun, setNodes, setEdges]);

  const onConnect = useCallback(
    (params: Connection) =>
      setEdges((eds) => addEdge({ ...params, animated: true, markerEnd: { type: MarkerType.ArrowClosed } }, eds)),
    [setEdges],
  )

  const handleNodeClick = (event: React.MouseEvent, node: any) => {
    if (onNodeSelect) {
      onNodeSelect(node.id)
    }
  }

  // Update node selection state when selectedNodeId changes
  useEffect(() => {
    if (selectedNodeId) {
      setNodes((nds) =>
        nds.map((node) => ({
          ...node,
          selected: node.id === selectedNodeId,
        })),
      )
    }
  }, [selectedNodeId, setNodes])

  return (
    <main
      className={`bg-white transition-all duration-300 ease-in-out ${isLeftSidebarExpanded && isRightSidebarExpanded
        ? "hidden md:block md:flex-1"
        : isLeftSidebarExpanded || isRightSidebarExpanded
          ? "flex-1 md:block"
          : "flex-1"
        }`}
    >
      <div className="h-full w-full">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onNodeClick={handleNodeClick}
          nodeTypes={nodeTypes}
          fitView
          connectionLineType={ConnectionLineType.SmoothStep}
          attributionPosition="bottom-right"
          nodesDraggable={false}
          nodesConnectable={false}
          edgesUpdatable={false}
          connectOnClick={false}
        >
          <Controls />
          <MiniMap />
          <Background color="#aaa" gap={16} size={1} variant={BackgroundVariant.Dots} />
        </ReactFlow>
      </div>
    </main>
  )
}
