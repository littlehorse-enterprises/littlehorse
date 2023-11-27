import type { NextApiRequest, NextApiResponse } from 'next'
import { handleGrpcCallWithNext } from '../grpcMethodCallHandler'
import { TaskDefId } from '../../../littlehorse-public-api/object_id'


export default async function handler(req: NextApiRequest, res: NextApiResponse) {
    if (req.method === 'POST') {
        const body = JSON.parse(req.body)
        const { id } = body

        await handleGrpcCallWithNext('getTaskDef', req, res, TaskDefId.fromJSON({
            name: id
        } as TaskDefId))
    }
}
