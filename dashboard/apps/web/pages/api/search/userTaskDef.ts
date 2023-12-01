import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchUserTaskDefRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchUserTaskDef', req, res, SearchUserTaskDefRequest.fromJSON(JSON.parse(req.body)))
    }
}
