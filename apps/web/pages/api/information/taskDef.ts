import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'


export default async function handler(req: NextApiRequest, res: NextApiResponse) {

    if (req.method === 'POST') {
        const body = JSON.parse(req.body)
        const taskDefName = body.id

        try {
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            const response = await client.getTaskDef({ name: taskDefName } as any)
            res.send({ result: response }) 


        } catch (error) {
            console.error('information/taskDef - Error during GRPC call:', error)
            res.send({
                error: `Something went wrong.${error}`,
            }) 
        }
    }
}
