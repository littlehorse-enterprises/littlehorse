import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchExternalEventDefRequest } from '../../../littlehorse-public-api/service'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    if (req.method === 'POST') {
        await handleGrpcCallWithNext('searchExternalEventDef', req, res, SearchExternalEventDefRequest.fromJSON(JSON.parse(req.body)))
    }
}
