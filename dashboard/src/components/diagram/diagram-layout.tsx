"use client"

import LeftSidebar from "@/components/diagram/left-sidebar/left-sidebar"
import RightSidebar from "@/components/diagram/right-sidebar"
import WorkflowDiagram from "@/components/diagram/workflow-diagram"
import { type Edge, type Node } from "@xyflow/react"
import { WfSpec, WfRun, NodeRun } from "littlehorse-client/proto"

interface DiagramLayoutProps {
    wfSpec: WfSpec
    wfRun?: WfRun
    nodeRuns?: NodeRun[]
    nodes: Node[]
    edges: Edge[]
}

export default function DiagramLayout({
    wfSpec,
    wfRun,
    nodeRuns = [],
    nodes,
    edges
}: DiagramLayoutProps) {
    return (
        <div className="flex h-full">
            <LeftSidebar wfSpec={wfSpec} wfRun={wfRun} />
            <div className="flex-1 flex">
                <WorkflowDiagram nodes={nodes} edges={edges} />
                <RightSidebar
                    wfSpec={wfSpec}
                    nodeRuns={nodeRuns}
                />
            </div>
        </div>
    )
}