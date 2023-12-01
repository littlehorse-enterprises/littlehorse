import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../../../grpcMethodCallHandler'
import { GetLatestWfSpecRequest } from '../../../../../littlehorse-public-api/service'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'GET') {
        await handleGrpcCallWithNext('getLatestWfSpec', req, res, GetLatestWfSpecRequest.fromJSON({ name: req.query.name }))
    }
}
