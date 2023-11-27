import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../../../grpcMethodCallHandler'
import { NodeRunId } from '../../../../../littlehorse-public-api/object_id'

export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const {
            id
        } = JSON.parse(req.body)

        const {
            number,
            position
        } = req.query

        await handleGrpcCallWithNext('getNodeRun', req, res, NodeRunId.fromJSON({
            wfRunId: id,
            threadRunNumber: Number(number),
            position: Number(position)
        } as NodeRunId))
    }
}
