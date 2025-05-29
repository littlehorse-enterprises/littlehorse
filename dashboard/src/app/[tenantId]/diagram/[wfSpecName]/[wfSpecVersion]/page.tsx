import LeftSidebar from "@/components/diagram/left-sidebar";
import { lhClient } from "@/lib/lhClient";
import { PageParams } from "@/types/PageParams";

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
    const wfRun = await client.getWfRun({ id: wfRunId });

    console.log('wfSpec', wfSpec);
    console.log('wfRun', wfRun);

    return <div className="h-full">
        <LeftSidebar wfSpec={wfSpec} wfRun={wfRun} />
    </div>;
}