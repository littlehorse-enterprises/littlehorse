import type { NextApiRequest, NextApiResponse } from 'next'
import { ListTaskMetricsRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('listTaskDefMetrics', req, res, ListTaskMetricsRequest.fromJSON(JSON.parse(req.body)))
    }
}