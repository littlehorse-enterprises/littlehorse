import { createMocks } from 'node-mocks-http'
import handler from '../../../../../pages/api/loops/taskRun'
import type { NextApiRequest, NextApiResponse } from 'next'
import * as grpcCallHandler from '../../../../../pages/api/grpcMethodCallHandler'
import { TaskStatus } from '../../../../../littlehorse-public-api/common_enums'
import type { SearchTaskRunRequest } from '../../../../../littlehorse-public-api/service'

jest.mock('../../../../../pages/api/grpcMethodCallHandler')

describe('taskRun API', () => {
    it('should get all the SCHEDULED RUNNING and SUCCESS task runs for the given wfRunId', async () => {
        const { req, res }: { req: NextApiRequest; res: NextApiResponse } = createMocks({ method: 'POST' })

        req.body = JSON.stringify({
            taskDefName: 'A_TASKDEF',
            wfRunId: 'A_WFRUN_ID'
        });

        (grpcCallHandler.makeGrpcCall as jest.Mock).mockImplementation((method: string, req: NextApiRequest, res: NextApiResponse, grpcRequestBody: SearchTaskRunRequest) => {
            if (grpcRequestBody.statusAndTaskDef?.status === TaskStatus.TASK_SCHEDULED) {
                expect(grpcRequestBody).toEqual( { statusAndTaskDef: {
                    status: 'TASK_SCHEDULED',
                    taskDefName: 'A_TASKDEF'
                },
                'limit': 99
                })

                return Promise.resolve({
                    results: [
                        { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_SCHEDULED_TASK_GUID' },
                        { wfRunId: 'ANOTHER_WFRUN_ID', taskGuid: 'ANOTHER_SCHEDULED_TASK_GUID' }
                    ]
                })
            }

            if (grpcRequestBody.statusAndTaskDef?.status === TaskStatus.TASK_RUNNING) {
                expect(grpcRequestBody).toEqual( { statusAndTaskDef: {
                    status: 'TASK_RUNNING',
                    taskDefName: 'A_TASKDEF'
                },
                'limit': 99
                })

                return Promise.resolve({
                    results: [
                        { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_RUNNING_TASK_GUID' },
                        { wfRunId: 'ANOTHER_WFRUN_ID', taskGuid: 'ANOTHER_RUNNING_TASK_GUID' }
                    ]
                })
            }

            if (grpcRequestBody.statusAndTaskDef?.status === TaskStatus.TASK_SUCCESS) {
                expect(grpcRequestBody).toEqual( { statusAndTaskDef: {
                    status: 'TASK_SUCCESS',
                    taskDefName: 'A_TASKDEF'
                },
                'limit': 99
                })

                return Promise.resolve({
                    results: [
                        { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_SUCCESS_TASK_GUID' },
                        { wfRunId: 'ANOTHER_WFRUN_ID', taskGuid: 'ANOTHER_SUCCESS_TASK_GUID' }
                    ]
                })
            }
        })

        const sendMock = jest.fn()
        res.send = sendMock

        await handler(req, res)

        expect(sendMock).toHaveBeenCalledWith([
            { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_SCHEDULED_TASK_GUID' },
            { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_RUNNING_TASK_GUID' },
            { wfRunId: 'A_WFRUN_ID', taskGuid: 'A_SUCCESS_TASK_GUID' }
        ])
    })
})
