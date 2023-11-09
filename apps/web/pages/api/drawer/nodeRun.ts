import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const body = JSON.parse(req.body)
        const { wfRunId, threadRunNumber, name } = body

        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()
      
            const response = await client.getNodeRun({ wfRunId, threadRunNumber, position: Number(name) } as any)
            res.send(response) 

        } catch (error) {
            res.send({
                error: `Drawer NodeRun - Something went wrong.${error}`,
            }) 
        }
    }
}
