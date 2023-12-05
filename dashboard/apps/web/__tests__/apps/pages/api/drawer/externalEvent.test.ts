import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/drawer/externalEvent'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { ExternalEventId } from '../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('externalEvent API', () => {
    it('should perform a grpc request for a externalEvent sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            wfRunId: {
                id: 'A_WFRUN_ID'
            },
            externalEventDefId: {
                name: 'AN_EXTERNAL_EVENT_NAME'
            },
            guid: 'A_GUID'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getExternalEvent', req, res, {
            wfRunId: {
                id: 'A_WFRUN_ID'
            },
            externalEventDefId: {
                name: 'AN_EXTERNAL_EVENT_NAME'
            },
            guid: 'A_GUID'
        } as ExternalEventId)
    })
})
