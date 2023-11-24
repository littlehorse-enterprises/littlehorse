import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/drawer/variable'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { VariableId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('taskDef API', () => {
    it('should perform a grpc request to search for a wfRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: 'A_WFRUN_ID',
            threadRunNumber: 0,
            name: 'A_VARIABLE_NAME'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getVariable', req, res, {
            wfRunId: 'A_WFRUN_ID',
            threadRunNumber: 0,
            name: 'A_VARIABLE_NAME'
        } as VariableId)
    })
})
