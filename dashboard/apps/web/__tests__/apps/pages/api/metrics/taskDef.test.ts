import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/metrics/taskDef'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import { MetricsWindowLength } from '../../../../../littlehorse-public-api/common_enums'
import type { ListTaskMetricsRequest } from '../../../../../littlehorse-public-api/service'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('metrics taskDef API', () => {
    it('should perform a grpc request to get the metrics for tasDefs sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            lastWindowStart: '2023-11-12T12:12:12Z',
            numWindows: 24,
            taskDefName: 'A_TASKDEF',
            windowLength: 'HOURS_2'
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('listTaskDefMetrics', req, res, {
            lastWindowStart: '2023-11-12T12:12:12Z',
            numWindows: 24,
            taskDefName: 'A_TASKDEF',
            windowLength: MetricsWindowLength.HOURS_2
        } as ListTaskMetricsRequest)
    })
})
