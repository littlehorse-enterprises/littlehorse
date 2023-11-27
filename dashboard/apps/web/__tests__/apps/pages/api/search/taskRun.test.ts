import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/search/taskRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import type { SearchTaskRunRequest } from '../../../../../littlehorse-public-api/service'
import { TaskStatus } from '../../../../../littlehorse-public-api/common_enums'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('TaskRun API', () => {
    afterEach(() => {
        jest.clearAllMocks()
    })

    it('should perform a grpc request to search for a taskRun sending the right request body', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })
        req.body = JSON.stringify({
            statusAndTaskDef: {
                status: 'TASK_RUNNING',
                taskDefName: 'A_TASK_DEF_NAME',
                earliestStart: '2022-11-11T12:12:12Z',
                latestStart: '2022-11-12T12:12:12Z',
            },
            bookmark: 'QV9CT09LTUFSSw==',
            limit: 5
        })

        await handler(req, res)

        expect(grpcCallHandler.handleGrpcCallWithNext).toHaveBeenCalledWith('searchTaskRun', req, res, {
            statusAndTaskDef: {
                status: TaskStatus.TASK_RUNNING,
                taskDefName: 'A_TASK_DEF_NAME',
                earliestStart: '2022-11-11T12:12:12Z',
                latestStart: '2022-11-12T12:12:12Z',
            },
            bookmark: Uint8Array.from([
                65, 95, 66, 79, 79,
                75, 77, 65, 82, 75
            ]),
            limit: 5
        } as SearchTaskRunRequest)
    })
})
