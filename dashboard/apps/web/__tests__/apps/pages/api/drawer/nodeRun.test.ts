import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/drawer/nodeRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { NodeRunId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('nodeRun API', () => {
    it('should perform a grpc request for a nodeRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: 'A_WFRUN_ID',
            threadRunNumber: 0,
            name: '1'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getNodeRun', req, res, {
            wfRunId: {
                id: 'A_WFRUN_ID'
            },
            threadRunNumber: 0,
            position: 1
        } as NodeRunId)
    })
})
