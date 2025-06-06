import { getWfRunDetails } from "@/actions/getWfRun";
import LeftSidebar from "@/components/diagram/left-sidebar/left-sidebar";
import WorkflowDiagram from "@/components/diagram/workflow-diagram";
import { extractEdgeData, extractNodeData } from "@/lib/data-extraction";
import { lhClient } from "@/lib/lhClient";
import { type Edge, type Node } from "@xyflow/react";

interface DiagramPageProps {
    params: Promise<{
        tenantId: string;
        wfSpecName: string;
        wfSpecVersion: string;
    }>;
    searchParams: Promise<{
        wfRunId?: string;
    }>;
}

export default async function DiagramPage({
    params,
    searchParams
}: DiagramPageProps) {
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
            // TODO: need to support wfSpec only display
            extractedNodes = extractNodeData(wfSpec);
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
