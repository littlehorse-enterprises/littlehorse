"use server";
import { lhClient } from "@/lib/lhClient";
import { WithTenant } from "@/types";
import { WfSpec } from "littlehorse-client/proto";

type GetWfSpecProps = {
  name: string;
  version: string;
} & WithTenant;

export const getWfSpec = async ({
  name,
  version,
  tenantId,
}: GetWfSpecProps): Promise<WfSpec> => {
  const client = await lhClient({ tenantId });

  if (/[0-9]+\.[0-9]+/.test(version)) {
    const [majorVersion, revision] = version.split(".");
    return client.getWfSpec({
      name,
      majorVersion: parseInt(majorVersion),
      revision: parseInt(revision),
    });
  }

  return client.getLatestWfSpec({ name });
};
