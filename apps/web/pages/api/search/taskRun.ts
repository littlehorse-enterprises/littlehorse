import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import { SearchTaskRunRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response = await client.searchTaskRun(SearchTaskRunRequest.fromJSON(JSON.parse(req.body)) as any)

            res.send(response) 

        } catch (error) {
            console.error(' search/taskRun - Error during GRPC call:', error)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
