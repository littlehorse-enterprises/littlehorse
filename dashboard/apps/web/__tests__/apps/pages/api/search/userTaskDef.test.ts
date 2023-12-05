import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/userTaskDef'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchUserTaskDefRequest } from '../../../../../littlehorse-public-api/service'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('user task def API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for a userTaskDef sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            prefix: 'A_PREFIX'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchUserTaskDef', req, res, {
            prefix: 'A_PREFIX'
        } as SearchUserTaskDefRequest)
    })
})
