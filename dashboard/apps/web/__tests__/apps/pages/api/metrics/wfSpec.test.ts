import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/metrics/wfSpec'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import { MetricsWindowLength } from '../../../../../littlehorse-public-api/common_enums'
import type { ListWfMetricsRequest } from '../../../../../littlehorse-public-api/service'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('metrics wfSpec API', () => {
    it('should perform a grpc request to get the metrics for wfSpecs sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            lastWindowStart: '2023-11-12T12:12:12Z',
            numWindows: 24,
            wfSpecName: 'A_WFSPEC',
            majorVersion: 1,
            revision: 1,
            windowLength: 'HOURS_2'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('listWfSpecMetrics', req, res, {
            wfSpecId: {
                name: 'A_WFSPEC',
                majorVersion: 1,
                revision: 1
            },
            lastWindowStart: '2023-11-12T12:12:12Z',
            windowLength: MetricsWindowLength.HOURS_2,
            numWindows: 24
        } as ListWfMetricsRequest)
    })
})
