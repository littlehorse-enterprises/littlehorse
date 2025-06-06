"use client";

import React, { useCallback, useEffect } from 'react';
import {
    ReactFlow,
    Background,
    Controls,
    MiniMap,
    type NodeTypes,
    useNodesState,
    useEdgesState,
    type Node,
    type Edge,
    BackgroundVariant,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import TaskNode from '@/components/flow/task-node';
import { mockNodes, mockEdges } from '@/lib/mock-data';
import { getLayoutedElements } from '@/lib/layout-utils';
import { useSelection } from '../context/selection-context';

// Define custom node types
const nodeTypes: NodeTypes = {
    task: TaskNode,
};

interface WorkflowDiagramProps {
    className?: string;
    nodes?: Node[];
    edges?: Edge[];
}

export default function WorkflowDiagram({
    className = '',
    nodes: customNodes,
    edges: customEdges
}: WorkflowDiagramProps) {
    const [nodes, setNodes, onNodesChange] = useNodesState<Node>([]);
    const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
    const { selectedId, setSelectedId } = useSelection();

    // Use custom nodes/edges if provided, otherwise use mock data
    const sourceNodes = customNodes || mockNodes;
    const sourceEdges = customEdges || mockEdges;

    // Apply layout when component mounts or when source data changes
    useEffect(() => {
        const applyLayout = async () => {
            const { nodes: layoutedNodes, edges: layoutedEdges } = await getLayoutedElements(
                sourceNodes,
                sourceEdges
            );
            setNodes(layoutedNodes);
            setEdges(layoutedEdges);
        };

        applyLayout();
    }, [sourceNodes, sourceEdges, setNodes, setEdges]);

    // Update node selection when selectedId changes
    useEffect(() => {
        setNodes((nds) =>
            nds.map((node) => ({
                ...node,
                selected: node.id === selectedId
            }))
        );
    }, [selectedId, setNodes]);

    const handleNodeClick = useCallback((event: React.MouseEvent, node: Node) => {
        setSelectedId(node.id);
    }, [setSelectedId]);

    return (
        <div className={`h-full w-full ${className}`}>
            <ReactFlow
                nodes={nodes}
                edges={edges}
                onNodesChange={onNodesChange}
                onEdgesChange={onEdgesChange}
                onNodeClick={handleNodeClick}
                nodeTypes={nodeTypes}
                fitView
                attributionPosition="bottom-right"
                nodesDraggable={false}
                nodesConnectable={false}
                connectOnClick={false}
                onPaneClick={() => setSelectedId(null)}
            >
                <Controls />
                <MiniMap />
                <Background
                    color="#aaa"
                    gap={16}
                    size={1}
                    variant={BackgroundVariant.Dots}
                />
            </ReactFlow>
        </div>
    );
} 