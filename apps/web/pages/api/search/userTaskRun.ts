import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { SearchUserTaskRunRequest } from '../../../littlehorse-public-api/service'


export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchUserTaskRun', req, res, SearchUserTaskRunRequest.fromJSON(JSON.parse(req.body)))
    }
}
