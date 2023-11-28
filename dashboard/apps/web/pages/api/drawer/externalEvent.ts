import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { ExternalEventId } from '../../../littlehorse-public-api/object_id'

export default async function handler(
    req: NextApiRequest,
    res: NextApiResponse
) {
    if (req.method === 'POST') {
        const httpRequestBody = JSON.parse(req.body)
        const grpcRequestBody = {
            wfRunId: httpRequestBody.wfRunId,
            externalEventDefId: httpRequestBody.externalEventDefId,
            guid: httpRequestBody.guid
        } as ExternalEventId

        await handleGrpcCallWithNext('getExternalEvent', req, res, ExternalEventId.fromJSON(grpcRequestBody))
    }
}
