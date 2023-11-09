import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import { SearchUserTaskDefRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response = await client.searchUserTaskDef(SearchUserTaskDefRequest.fromJSON(req.body) as any)

            res.send(response) 
        } catch (error) {
            console.error('search/userTaskDef - Error during GRPC call:', error)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
