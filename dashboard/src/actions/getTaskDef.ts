"use server";

import { lhClient } from "@/lib/lhClient";
import { TaskDefId } from "littlehorse-client/proto";

export const getTaskDef = async (tenantId: string, request: TaskDefId) => {
  const client = await lhClient({ tenantId });

  return client.getTaskDef(request);
};
