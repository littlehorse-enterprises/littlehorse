import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/userTaskRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchUserTaskRunRequest } from '../../../../../littlehorse-public-api/service'
import { UserTaskRunStatus } from '../../../../../littlehorse-public-api/user_tasks'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('user task run API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for a user task run sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })

        req.body = JSON.stringify({
            status: 'ASSIGNED',
            userTaskDefName: 'A_DEF_NAME',
            earliestStart: '2023-11-11T12:12:12Z',
            latestStart: '2023-11-12T12:12:12Z',
            limit: 5,
            bookmark: 'QV9CT09LTUFSSw=='
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchUserTaskRun', req, res, {
            status: UserTaskRunStatus.ASSIGNED,
            userTaskDefName: 'A_DEF_NAME',
            earliestStart: '2023-11-11T12:12:12Z',
            latestStart: '2023-11-12T12:12:12Z',
            limit: 5,
            bookmark: Uint8Array.from([
                65, 95, 66, 79, 79,
                75, 77, 65, 82, 75
            ]),
        } as SearchUserTaskRunRequest)
    })
})
