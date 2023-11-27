import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/information/userTaskDef'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { UserTaskDefId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('information userTaskDef API', () => {
    it('should perform a grpc request to get information for a userTaskDef sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            id: 'A_TASKDEF',
            version: 0
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getUserTaskDef', req, res, {
            name: 'A_TASKDEF', 
            version: 0 
        } as UserTaskDefId)
    })
})