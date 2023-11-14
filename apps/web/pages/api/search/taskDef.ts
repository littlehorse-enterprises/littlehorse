import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import { SearchTaskDefRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'
import { getServerSession } from 'next-auth/next'
import { authOptions } from '../auth/[...nextauth]'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {


    if (req.method === 'POST') {
        const session = await getServerSession(req, res, authOptions)

        if (session) {
            try {
                const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

                const response = await client.searchTaskDef(SearchTaskDefRequest.fromJSON(req.body) as any)

                res.send(response)
            } catch (error) {
                console.error('search/taskDef - Error during GRPC call:', error)
                res.send({
                    error: `Something went wrong.${error}`,
                })
            }
        } else {
            res.status(401)
                .json({
                    status: 401,
                    message: 'You need to be authenticated to access this resource.'
                })
        }
    }
}
