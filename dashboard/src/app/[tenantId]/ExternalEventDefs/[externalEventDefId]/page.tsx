import { lhClient } from "@/utils/client/lhClient";
import ExternalEventDefClient from "./ExternalEventDefClient";

interface ExternalEventDefPageProps {
  params: Promise<{
    tenantId: string;
    externalEventDefId: string;
  }>;
}

export default async function ExternalEventDefPage({ params }: ExternalEventDefPageProps) {
  const { tenantId, externalEventDefId } = await params;
  const client = await lhClient(tenantId);
  const externalEventDef = await client.getExternalEventDef({
    name: externalEventDefId,
  });
  return (
    <ExternalEventDefClient externalEventDef={externalEventDef} />
  );
}
