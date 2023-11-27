import type { RequestMethod } from 'node-mocks-http'
import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/wfSpec'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchWfSpecRequest } from '../../../../../littlehorse-public-api/service'
jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('wfSpec API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to get the wfSpec sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })

        req.body = JSON.stringify({ prefix: 'any_prefix' })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext)
            .toHaveBeenCalledWith('searchWfSpec', req, res, { prefix: 'any_prefix' } as SearchWfSpecRequest)
    })
})