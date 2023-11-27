import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/drawer/userTaskRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { UserTaskRunId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('drawer userTaskRun API', () => {
    it('should perform a grpc request for a userTaskRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: 'A_WFRUN_ID',
            guid: 'A_GUID'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getUserTaskRun', req, res, {
            wfRunId: 'A_WFRUN_ID',
            userTaskGuid: 'A_GUID'
        } as UserTaskRunId)
    })
})