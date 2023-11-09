import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'
import type { Variable } from '../../../littlehorse-public-api/variable'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const body = JSON.parse(req.body)

        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response: Variable = await client.getVariable(body)
            res.json({ code: 'OK', data: { result: response } }) 


        } catch (error) {
            console.error('drawer/variable - Error during GRPC call:', error)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
