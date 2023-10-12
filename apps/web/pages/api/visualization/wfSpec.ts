import { NextApiRequest, NextApiResponse } from "next";
import { createChannel, createClient } from 'nice-grpc';
import { LHPublicApiDefinition } from "../../../littlehorse-public-api/service";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

  if (req.method === 'POST') {
    //TODO: Insecure channel needs to be changed by a secure one
    const channel = createChannel(process.env.API_URL!!);

    const client = createClient(LHPublicApiDefinition, channel);

    try {
      const parsedRequestBody = JSON.parse(req.body)
      const response = await client.getWfSpec({name: parsedRequestBody.id, version: parsedRequestBody.version} as any);
      return res.send(response)
    } catch (error) {
      console.log("Error during GRPC call:", error);
      return res.send({
        error: "Something went wrong." + error,
      })
    }
  }
}
