import { createMocks } from 'node-mocks-http'
import handler from '../../../../../../../../web/pages/api/drawer/wfSpec/[name]/index'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../../../../../apps/web/pages/api/grpcMethodCallHandler'
import type { GetLatestWfSpecRequest } from '../../../../../../../littlehorse-public-api/object_id'

jest.mock('../../../../../../../../../apps/web/pages/api/grpcMethodCallHandler')

describe('wfSpec API', () => {
    it('should perform a grpc request for a wfSpec sending the right request body', async () => {


        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'GET' })
        req.query = {
            name: 'A_WFSPEC_NAME'
        }

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('getLatestWfSpec', req, res, {
            name: 'A_WFSPEC_NAME'
        } as GetLatestWfSpecRequest)
    })
})
