import { NextApiRequest, NextApiResponse } from "next";
import { createClient } from "nice-grpc/lib/client/ClientFactory";
import { createChannel } from "nice-grpc/lib/client/channel";
import { LHPublicApiDefinition } from "../../../littlehorse-public-api/service";

export default async function handler(
  req: NextApiRequest,
  res: NextApiResponse
) {
  if (req.method === "POST") {
    const body = JSON.parse(req.body);
    const { wfRunId, guid } = body;
    try {
      const channel = createChannel(process.env.API_URL!!);
      const client = createClient(LHPublicApiDefinition, channel);

      const response = await client.getUserTaskRun({ wfRunId, userTaskGuid: guid } as any);
      console.log("REsponse getting userTaskRun::::::", response)
      return res.send(response);

    } catch (error) {
      console.log("userTaskRun.ts - Error during GRPC call:", error);
      return res.send({
        error: "Something went wrong." + error,
      })
    }

  }
}
