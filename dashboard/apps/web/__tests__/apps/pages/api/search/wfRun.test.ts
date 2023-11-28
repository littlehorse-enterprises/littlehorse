import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/wfRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchWfRunRequest } from '../../../../../littlehorse-public-api/service'
import { LHStatus } from '../../../../../littlehorse-public-api/common_enums'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('WFRun API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for a wfRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            statusAndSpec: {
                status: 'RUNNING',
                wfSpecId: {
                    name: 'any_wfSpec_name',
                    majorVersion: 0
                },
                earliestStart: '2023-11-11T12:12:12Z',
                latestStart: '2023-11-11T14:12:12Z'
            },
            bookmark: 'QV9CT09LTUFSSw==',
            limit: 4
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchWfRun', req, res, {
            statusAndSpec: {
                wfSpecId: {
                    name: 'any_wfSpec_name',
                    majorVersion: 0,
                    revision: 0
                },
                status: LHStatus.RUNNING,
                earliestStart: '2023-11-11T12:12:12Z',
                latestStart: '2023-11-11T14:12:12Z'
            },
            bookmark: Uint8Array.from([
                65, 95, 66, 79, 79,
                75, 77, 65, 82, 75
            ]),
            limit: 4
        } as SearchWfRunRequest)
    })
})
