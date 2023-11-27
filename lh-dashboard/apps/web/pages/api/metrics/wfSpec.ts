import type { NextApiRequest, NextApiResponse } from 'next'
import { ListWfMetricsRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('listWfSpecMetrics', req, res, ListWfMetricsRequest.fromJSON(JSON.parse(req.body)))
    }
}