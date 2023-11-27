import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/drawer/taskRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { TaskRunId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('taskRun API', () => {
    it('should perform a grpc request for a taskRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: 'A_WFRUN_ID',
            taskGuid: 'A_GUID'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getTaskRun', req, res, {
            wfRunId: 'A_WFRUN_ID',
            taskGuid: 'A_GUID'
        } as TaskRunId)
    })
})
