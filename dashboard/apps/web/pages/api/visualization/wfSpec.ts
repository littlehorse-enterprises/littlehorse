import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import type { WfSpecId } from '../../../littlehorse-public-api/object_id'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const parsedRequestBody = JSON.parse(req.body)
        const grpcRequestBody = {
            name: parsedRequestBody.id,
            version: parsedRequestBody.version
        } as WfSpecId

        await handleGrpcCallWithNext('getWfSpec', req, res, grpcRequestBody)
    }
}
