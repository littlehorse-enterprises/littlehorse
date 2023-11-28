import { createMocks } from 'node-mocks-http'

import type { NextApiRequest, NextApiResponse } from 'next'
import handler from '../../../../../../../pages/api/search/nodeRun/[number]/[position]'
import * as grpcCallHandler from '../../../../../../../pages/api/grpcMethodCallHandler'
import type { NodeRunId } from '../../../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../../../pages/api/grpcMethodCallHandler')

describe('nodeRun API', () => {
    it('should perform a grpc request to search for an externalEvent sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.query = {
            number: '0',
            position: '1'
        }

        req.body = JSON.stringify({
            id: 'A_WFRUN_ID',
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
