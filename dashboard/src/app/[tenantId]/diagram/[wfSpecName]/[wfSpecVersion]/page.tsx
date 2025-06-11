import { getWfRunDetails } from "@/actions/getWfRun";
import DiagramLayout from "@/components/diagram/diagram-layout";
import { extractEdgeData, extractNodeData } from "@/utils/data/data-extraction";
import { lhClient } from "@/utils/client/lhClient";
import { type Edge, type Node } from "@xyflow/react";
import { SelectionProvider } from "@/components/context/selection-context";
import { WfRunDetails } from "@/types/wfRunDetails";

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
    const { tenantId, wfSpecName, wfSpecVersion } = await params;
    const { wfRunId } = await searchParams;

    const client = await lhClient(tenantId);
    const splitVersion = wfSpecVersion.split('.');
    const wfSpec = await client.getWfSpec({
        name: wfSpecName,
        majorVersion: parseInt(splitVersion[0]),
        revision: parseInt(splitVersion[1])
    });

    let wfRunDetails: WfRunDetails | undefined;
    if (wfRunId) {
        wfRunDetails = await getWfRunDetails({ wfRunId: { id: wfRunId }, tenantId });
    }

    const nodes: Node[] = extractNodeData(wfSpec, wfRunDetails);
    const edges: Edge[] = extractEdgeData(wfSpec);

    return (
        <SelectionProvider>
            <DiagramLayout
                wfSpec={wfSpec}
                wfRun={wfRunDetails?.wfRun}
                nodeRuns={wfRunDetails?.nodeRuns}
                taskRuns={wfRunDetails?.taskRuns}
                nodes={nodes}
                edges={edges}
            />
        </SelectionProvider>
    );
}
