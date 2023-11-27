import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { UserTaskDefId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const body = JSON.parse(req.body)
        const { id, version } = body

        await handleGrpcCallWithNext('getUserTaskDef', req, res, UserTaskDefId.fromJSON({
            name: id,
            version: version
        }))
    }
}
