import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { VariableId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('getVariable', req, res, VariableId.fromJSON(JSON.parse(req.body)))
    }
}
