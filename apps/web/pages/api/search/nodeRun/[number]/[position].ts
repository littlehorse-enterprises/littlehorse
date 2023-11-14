import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../../../littlehorse-public-api/service'
import LHClient from '../../../LHClient'
import { getServerSession } from 'next-auth/next'
import { authOptions } from '../../../auth/[...nextauth]'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {


    if (req.method === 'POST') {
        const session = await getServerSession(req, res, authOptions)

        if (session) {
            const {
                id
            } = JSON.parse(req.body)


            const {
                number,
                position
            } = req.query

            try {
                const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

                const response = await client.getNodeRun({
                    wfRunId: id,
                    threadRunNumber: Number(number),
                    position: Number(position)
                } as any)

                res.send(response)
            } catch (error) {
                console.error('search/nodeRun/number - Error during GRPC call:', error)
                res.status(500)
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
