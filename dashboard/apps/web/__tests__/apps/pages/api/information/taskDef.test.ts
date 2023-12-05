import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/information/taskDef'
import type { NextApiRequest, NextApiResponse } from 'next'
import type { TaskDefId } from '../../../../../littlehorse-public-api/object_id'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('information taskDef API', () => {
    it('should perform a grpc request to get information for a taskDef sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            id: 'A_TASKDEF'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getTaskDef', req, res, {
            name: 'A_TASKDEF'
        } as TaskDefId)
    })
})
