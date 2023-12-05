import type { NextApiRequest, NextApiResponse } from 'next'
import { ListTaskMetricsRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const httpRequestBody = (JSON.parse(req.body))
        const grpcRequestBody = {
            lastWindowStart: httpRequestBody.lastWindowStart,
            numWindows: httpRequestBody.numWindows,
            taskDefId: {
                name:  httpRequestBody.taskDefName
            },
            windowLength: httpRequestBody.windowLength
        } as ListTaskMetricsRequest
        await handleGrpcCallWithNext('listTaskDefMetrics', req, res, ListTaskMetricsRequest.fromJSON(grpcRequestBody))
    }
}
