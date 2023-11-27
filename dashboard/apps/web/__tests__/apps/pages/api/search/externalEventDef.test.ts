import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/externalEventDef'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchExternalEventDefRequest } from '../../../../../littlehorse-public-api/service'
jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('externalEventDef API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for an externalEvent sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            prefix: 'A_PREFIX',
            bookmark: 'QV9CT09LTUFSSw==',
            limit: 5
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchExternalEventDef', req, res, {
            prefix: 'A_PREFIX',
            bookmark: Uint8Array.from([
                65, 95, 66, 79, 79,
                75, 77, 65, 82, 75
            ]),
            limit: 5
        } as SearchExternalEventDefRequest)
    })
})
