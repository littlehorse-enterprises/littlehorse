import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../../../grpcMethodCallHandler'
import { TaskDefId } from '../../../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'GET') {
        await handleGrpcCallWithNext('getTaskDef', req, res, TaskDefId.fromJSON({ name: req.query.name }))
    }
}
