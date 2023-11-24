import type { NextApiRequest, NextApiResponse } from 'next'
import { ListNodeRunsRequest } from '../../../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../../../grpcMethodCallHandler'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'GET') {
        await handleGrpcCallWithNext('listNodeRuns', req, res, ListNodeRunsRequest.fromJSON({ wfRunId: req.query.id }))
    }
}
