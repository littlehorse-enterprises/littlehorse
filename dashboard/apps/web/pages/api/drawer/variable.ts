import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { VariableId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const httpRequestBody = JSON.parse(req.body)
        const grpcRequestBody = {
            wfRunId: {
                id: httpRequestBody.wfRunId,
            },
            threadRunNumber: httpRequestBody.threadRunNumber,
            name: httpRequestBody.name
        } as VariableId
        await handleGrpcCallWithNext('getVariable', req, res, VariableId.fromJSON(grpcRequestBody))
    }
}
