import { ThreadRunWithNodeRuns } from "@/app/actions/getWfRun";
import { WfRun } from "littlehorse-client/proto";
import { FC, useCallback, useEffect } from "react";
import dagre from '@dagrejs/dagre';
import { Node, Edge, useStore, Position } from "@xyflow/react";

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
            position: {
                x: nodeWithPosition.x - nodeWidth / 2,
                y: nodeWithPosition.y - nodeHeight / 2,
            },
        };

        return newNode;
    });

    return { nodes: newNodes, edges };
};

export const Layouter: FC<{ wfRun?: WfRun & { threadRuns: ThreadRunWithNodeRuns[] }; nodeRunNameToBeHighlighted?: string }> = ({
    wfRun,
    nodeRunNameToBeHighlighted,
}) => {
    const nodes = useStore(store => store.nodes);
    const edges = useStore(store => store.edges);
    const setNodes = useStore(store => store.setNodes);
    const layoutedElements = getLayoutedElements(nodes, edges)

    const onLoad = useCallback(() => {
        setNodes(layoutedElements.nodes)
    }, [setNodes])

    useEffect(() => {
        onLoad()
    }, [onLoad, nodes, edges])

    return <></>
}