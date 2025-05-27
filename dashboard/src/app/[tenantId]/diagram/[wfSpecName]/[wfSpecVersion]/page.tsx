import { executeRpc } from "@/actions/executeRPC";
import { lhClient } from "@/lib/lhClient";
import { PageParams } from "@/types/PageParams";

export default async function DiagramPage({
    params,
    searchParams,
}: PageParams) {
    // Access filters parameter
    const param = (await params);
    console.log(param);
    const wfSpecId = (await searchParams).wfSpecId;
    console.log(wfSpecId);

    // const wfSpec = await executeRpc("whoami", {}, param.tenantId);
    // console.log(wfSpec);
    const client = await lhClient(param.tenantId);
    const whoami = await client.whoami({});
    console.log(whoami);

    return <div>Dashboard</div>;
}