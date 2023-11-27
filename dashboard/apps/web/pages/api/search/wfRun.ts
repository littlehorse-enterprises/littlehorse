import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { SearchWfRunRequest } from '../../../littlehorse-public-api/service'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    if (req.method === 'POST'){
        await handleGrpcCallWithNext('searchWfRun', req, res, SearchWfRunRequest.fromJSON(JSON.parse(req.body)))
    }
}
