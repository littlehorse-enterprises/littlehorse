import { NextApiRequest, NextApiResponse } from "next";
import { createChannel, createClient } from 'nice-grpc';
import { LHPublicApiDefinition, SearchExternalEventDefRequest, SearchTaskDefRequest } from "../../../littlehorse-public-api/service";

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
      if(req.method === 'POST'){
          try {
            const channel = createChannel(process.env.API_URL!!);
            const client = createClient(LHPublicApiDefinition, channel);

            const response = await client.searchExternalEventDef(SearchExternalEventDefRequest.fromJSON(req.body) as any);

            return res.send(response);
        } catch (error) {
            console.log("Error during GRPC call:", error);
            return res.send({
                error: "Something went wrong." + error,
            })
        }
      }
}
