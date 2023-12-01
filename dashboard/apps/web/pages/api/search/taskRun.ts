import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchTaskRunRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchTaskRun', req, res, SearchTaskRunRequest.fromJSON(JSON.parse(req.body)))
    }
}
