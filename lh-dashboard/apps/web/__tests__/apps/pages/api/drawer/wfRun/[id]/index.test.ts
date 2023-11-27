import { createMocks } from 'node-mocks-http'
import handler from '../../../../../../../../web/pages/api/drawer/wfRun/[id]/index'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../../../pages/api/grpcMethodCallHandler'
import type { ListNodeRunsRequest } from '../../../../../../../littlehorse-public-api/service'

jest.mock('../../../../../../../../../apps/web/pages/api/grpcMethodCallHandler')

describe('wfRun API', () => {
    it('should perform a grpc request for a wfRun sending the right request body', async () => {


        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'GET' })
        req.query = {
            id: 'A_WFRUN_ID'
        }

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('listNodeRuns', req, res, {
            wfRunId: 'A_WFRUN_ID'
        } as ListNodeRunsRequest)
    })
})