import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/visualization/wfSpec'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { WfSpecId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('WfSpec API', () => {
    it('should perform a grpc request to get the wfSpec sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({ id: 'any_wf_spec', version: 0 })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext)
            .toHaveBeenCalledWith('getWfSpec',
                req, res, {
                    name: 'any_wf_spec',
                    majorVersion: 0,
                    revision: 0 // TODO: OSS - bring this from the UI
                } as WfSpecId)
    })
})
