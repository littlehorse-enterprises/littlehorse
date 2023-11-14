import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'
import type { WfSpec } from '../../../littlehorse-public-api/wf_spec'
import { getServerSession } from 'next-auth/next'
import { authOptions } from '../auth/[...nextauth]'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

    if (req.method === 'POST') {
        const session = await getServerSession(req, res, authOptions)

        if (session) {
            //TODO: Insecure channel needs to be changed by a secure one
            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            try {
                const parsedRequestBody = JSON.parse(req.body)
                const response: WfSpec = await client.getWfSpec({
                    name: parsedRequestBody.id,
                    version: parsedRequestBody.version
                } as any)
                res.send(response)
            } catch (error) {
                console.error('visualization/WfSpec - Error during GRPC call:', error)
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
