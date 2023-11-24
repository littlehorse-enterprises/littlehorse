import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchTaskDefRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchTaskDef', req, res, SearchTaskDefRequest.fromJSON(JSON.parse(req.body)))
    }
}
