import { createMocks } from 'node-mocks-http'
import handler from '../../../../../../../../web/pages/api/drawer/taskDef/[name]/index'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../../../pages/api/grpcMethodCallHandler'
import type { TaskDefId } from '../../../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../../../../../apps/web/pages/api/grpcMethodCallHandler')

describe('taskDef API', () => {
    it('should perform a grpc request for a taskDef sending the right request body', async () => {


        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'GET' })
        req.query = {
            name: 'A_TASKDEF'
        }

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getTaskDef', req, res, {
            name: 'A_TASKDEF'
        } as TaskDefId)
    })
})
