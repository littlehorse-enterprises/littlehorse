import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/nodeRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchNodeRunRequest } from '../../../../../littlehorse-public-api/service'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('nodeRun API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for a nodeRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: 'A_WFRUN_ID',
            bookmark: 'QV9CT09LTUFSSw==',
            limit: 5
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchNodeRun', req, res, {
            bookmark: Uint8Array.from([
                65, 95, 66, 79, 79,
                75, 77, 65, 82, 75
            ]),
            limit: 5,
            wfRunId: 'A_WFRUN_ID'
        } as SearchNodeRunRequest)
    })
})