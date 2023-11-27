import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {

    if (req.method === 'POST'){
        
        const parsedRequestBody = JSON.parse(req.body)
        const grpcRequestBody = {
            id: parsedRequestBody.wfRunId
        }

        await handleGrpcCallWithNext('getWfRun', req, res, grpcRequestBody)
    }
}
