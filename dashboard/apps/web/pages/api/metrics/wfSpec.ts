import type { NextApiRequest, NextApiResponse } from 'next'
import { ListWfMetricsRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const httpRequestBody = JSON.parse(req.body)

        const grpcRequestBody = {
            wfSpecId: {
                name: httpRequestBody.wfSpecName,
                majorVersion: httpRequestBody.wfSpecVersion,
                revision: 0 // TODO: OSS - bring this from the UI
            },
            lastWindowStart: httpRequestBody.lastWindowStart,
            windowLength: httpRequestBody.windowLength,
            numWindows: httpRequestBody.numWindows
        } as ListWfMetricsRequest
        await handleGrpcCallWithNext('listWfSpecMetrics', req, res, ListWfMetricsRequest.fromJSON(grpcRequestBody))
    }
}
