import LeftSidebar from "@/components/diagram/left-sidebar/left-sidebar";
import WorkflowDiagram from "@/components/diagram/workflow-diagram";
import { lhClient } from "@/lib/lhClient";
import { PageParams } from "@/types/PageParams";
import { extractNodeData, extractEdgeData } from "@/lib/data-extraction";
import { type Node, type Edge } from "@xyflow/react";
import { getWfRunDetails } from "@/actions/getWfRun";

export default async function DiagramPage({
    params,
    searchParams
}: PageParams) {
    const { tenantId, wfSpecName, wfSpecVersion } = (await params);
    const { wfRunId } = (await searchParams);

    const splitVersion = wfSpecVersion.split('.');
    const majorVersion = parseInt(splitVersion[0]);
    const revision = parseInt(splitVersion[1]);

    const client = await lhClient(tenantId);
    const wfSpec = await client.getWfSpec({ name: wfSpecName, majorVersion: majorVersion, revision: revision });
    const wfRunDetails = wfRunId ? await getWfRunDetails({ wfRunId: { id: wfRunId }, tenantId: tenantId }) : undefined;

    // Extract nodes and edges from the workflow data
    let extractedNodes: Node[] = [];
    let extractedEdges: Edge[] = [];

    try {
        if (wfRunDetails) {
            extractedNodes = extractNodeData(wfSpec, wfRunDetails);
            extractedEdges = extractEdgeData(wfSpec);
        } else if (wfSpec) {
            // todo : need to support wfSpec only display
            extractedNodes = extractNodeData(wfSpec, wfSpecData);
            extractedEdges = extractEdgeData(wfSpec);
        }
    } catch (error) {
        console.warn("Error extracting workflow data:", error);
        // Fall back to empty arrays if extraction fails
        extractedNodes = [];
        extractedEdges = [];
    }

    return (
        <div className="h-full flex">
            <LeftSidebar wfSpec={wfSpec} wfRun={wfRunDetails?.wfRun} />
            <div className="flex-1">
                <WorkflowDiagram
                    nodes={extractedNodes}
                    edges={extractedEdges}
                />
            </div>
        </div>
    );
}
