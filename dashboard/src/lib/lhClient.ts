"use server";

import { auth } from "@/auth";
import { LHConfig } from "littlehorse-client";

const CONFIG = {
  apiHost: process.env.LHC_API_HOST || "localhost",
  apiPort: process.env.LHC_API_PORT || "2023",
  protocol: process.env.LHC_API_PROTOCOL || "PLAINTEXT",
  caCert: process.env.LHC_CA_CERT,
};

export async function lhClient(tenantId: string) {
  const session = await auth();
  const accessToken = session?.accessToken;

  const config = LHConfig.from({
    ...CONFIG,
    tenantId,
  });

  return config.getClient(accessToken);
}
