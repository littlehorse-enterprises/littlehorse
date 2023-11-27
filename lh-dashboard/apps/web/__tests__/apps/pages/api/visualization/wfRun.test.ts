import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/visualization/wfRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
jest.mock('../../../../../pages/api/grpcMethodCallHandler')
describe('WFRun API', () => {
    it('should perform a grpc request to get the wfRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({ wfRunId: 'any_wf_run_id' })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext)
            .toHaveBeenCalledWith('getWfRun',
                req, res, {
                    id: 'any_wf_run_id'
                })
    })
})
