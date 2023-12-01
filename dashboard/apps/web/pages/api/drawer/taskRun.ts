import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { TaskRunId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const httpRequestBody = JSON.parse(req.body)
        const grpcRequestBody = {
            wfRunId: httpRequestBody.wfRunId,
            taskGuid: httpRequestBody.taskGuid
        } as TaskRunId

        await handleGrpcCallWithNext('getTaskRun', req, res, TaskRunId.fromJSON(grpcRequestBody))
    }
}
