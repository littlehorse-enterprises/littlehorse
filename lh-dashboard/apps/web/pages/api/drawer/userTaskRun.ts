import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { UserTaskRunId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const { wfRunId, guid } = JSON.parse(req.body)
        await handleGrpcCallWithNext('getUserTaskRun', req, res, UserTaskRunId.fromJSON({ wfRunId, userTaskGuid: guid }))
    }
}
