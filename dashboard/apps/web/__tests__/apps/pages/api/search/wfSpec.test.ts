import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/wfSpec'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchWfSpecRequest } from '../../../../../littlehorse-public-api/service'
import LHClient from '../../../../../pages/api/LHClient'
import type { WfSpecIdList } from '../../../../../littlehorse-public-api/service'
jest.mock('../../../../../pages/api/LHClient')

describe('wfSpec API', () => {
    beforeEach(() => {
        process.env.LDH_OAUTH_ENABLED = 'false'
    })

    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to get the wfSpec sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })

        req.body = JSON.stringify({ prefix: 'any_prefix' });

        (LHClient.getInstance as unknown as jest.Mock).mockImplementation(() => ({
            searchWfSpec: (_: SearchWfSpecRequest): Promise<WfSpecIdList> => {
                return Promise.resolve({
                    results: [ { name: 'ANY_SPEC', majorVersion: 1, revision: 2 } ]
                } as WfSpecIdList)
            }
        }))

        const mockedJson = jest.fn()
        res.json = mockedJson

        await handler(req, res)

        expect(mockedJson).toHaveBeenCalledWith({
            results: [ { name: 'ANY_SPEC', majorVersion: 1, revision: 2, version: '1.2' } ]
        })
    })
})
