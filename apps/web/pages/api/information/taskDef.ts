import { NextApiRequest, NextApiResponse } from "next";
import { createChannel, createClient } from 'nice-grpc';
import { LHPublicApiDefinition } from "../../../littlehorse-public-api/service";


export default async function handler(req: NextApiRequest, res: NextApiResponse) {

  if (req.method === 'POST') {
    const body = JSON.parse(req.body);
    const taskDefName = body.id;

    try {
      const channel = createChannel(process.env.API_URL!!);
      const client = createClient(LHPublicApiDefinition, channel);

      const response = await client.getTaskDef({ name: taskDefName } as any);
      return res.send({ result: response })


    } catch (error) {
      console.log("Error during GRPC call:", error);
      return res.send({
        error: "Something went wrong." + error,
      })
    }
  }
}
