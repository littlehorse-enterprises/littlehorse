import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchNodeRunRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const requestBody = JSON.parse(req.body)

        await handleGrpcCallWithNext('searchNodeRun', req, res, SearchNodeRunRequest.fromJSON({
            bookmark: requestBody.bookmark,
            limit: requestBody.limit,
            wfRunId: {
                id: requestBody.wfRunId
            }
        }))
    }
}
