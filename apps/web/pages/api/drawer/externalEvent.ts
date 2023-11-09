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
        const { wfRunId, guid, externalEventDefName } = body
        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response = await client.getExternalEvent({ wfRunId, externalEventDefName, guid } as any)

            res.send(response) 
        } catch (error) {
            console.error(' external Event - Error during GRPC call:', error)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
