import { NextApiRequest, NextApiResponse } from "next";
import { createClient } from "nice-grpc/lib/client/ClientFactory";
import { createChannel } from "nice-grpc/lib/client/channel";
import { LHPublicApiDefinition } from "../../../../../littlehorse-public-api/service";

export default async function handler(req: NextApiRequest, res: NextApiResponse) {


    if (req.method === 'POST') {
        const {
            id
        } = JSON.parse(req.body);


        const {
            number,
            position
        } = req.query;

        try {
            const channel = createChannel(process.env.API_URL!!);
            const client = createClient(LHPublicApiDefinition, channel);

            const response = await client.getNodeRun({ wfRunId: id, threadRunNumber: Number(number), position: Number(position) } as any);

            return res.send(response);
        } catch (error) {
            console.log("Error during GRPC call:", error);
            res.status(404);
            return res.send({
                error: "Something went wrong." + error,
            })
        }
    }
}
