import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../../../littlehorse-public-api/service'
import LHClient from '../../../LHClient'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {


    if (req.method === 'POST') {
        const {
            id
        } = JSON.parse(req.body)


        const {
            number,
            position
        } = req.query

        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response = await client.getNodeRun({ wfRunId: id, threadRunNumber: Number(number), position: Number(position) } as any)

            res.send(response) 
        } catch (error) {
            console.error('search/nodeRun/number - Error during GRPC call:', error)
            res.status(404)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
