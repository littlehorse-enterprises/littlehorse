import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { SearchWfSpecRequest } from '../../../littlehorse-public-api/service'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchWfSpec', req, res, SearchWfSpecRequest.fromJSON(JSON.parse(req.body)))
    }
}
