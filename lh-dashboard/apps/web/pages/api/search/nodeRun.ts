import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchNodeRunRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchNodeRun', req, res, SearchNodeRunRequest.fromJSON(JSON.parse(req.body)))
    }
}
