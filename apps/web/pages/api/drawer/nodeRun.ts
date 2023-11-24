import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { NodeRunId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const body = JSON.parse(req.body)
        const { wfRunId, threadRunNumber, name } = body
        await handleGrpcCallWithNext('getNodeRun', req, res, NodeRunId.fromJSON({
            wfRunId, threadRunNumber, position: Number(name) 
        } as NodeRunId))
    }
}
