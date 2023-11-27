import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { ExternalEventId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('getExternalEvent', req, res, ExternalEventId.fromJSON(JSON.parse(req.body)))
    }
}
