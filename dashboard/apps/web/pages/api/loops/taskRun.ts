import type { NextApiRequest, NextApiResponse } from 'next'
import { SearchTaskRunRequest } from '../../../littlehorse-public-api/service'
import { makeGrpcCall } from '../grpcMethodCallHandler'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    if (req.method === 'POST') {
        // const session = await getServerSession(req, res, authOptions)

        // if (session) {
        // - TASK_SCHEDULED
        // - TASK_RUNNING
        // - TASK_SUCCESS
        // - TASK_FAILED
        // - TASK_TIMEOUT
        // - TASK_OUTPUT_SERIALIZING_ERROR
        // - TASK_INPUT_VAR_SUB_ERROR"
        const body = JSON.parse(req.body)
        const { taskDefName, wfRunId } = body
        let out = []

        const scheduledTasks = await makeGrpcCall('searchTaskRun',
            req,
            res,
            SearchTaskRunRequest.fromJSON({
                statusAndTaskDef: {
                    status: 'TASK_SCHEDULED',
                    taskDefName
                },
                limit: 99
            }))

        out = out.concat(scheduledTasks.results.filter(r => r.wfRunId === wfRunId) )

        const runningTasks = await makeGrpcCall('searchTaskRun',
            req,
            res,
            SearchTaskRunRequest.fromJSON({
                statusAndTaskDef: {
                    status: 'TASK_RUNNING',
                    taskDefName
                },
                limit: 99
            }))
        out = out.concat(runningTasks.results.filter(r => r.wfRunId === wfRunId) )

        const successfulTasks = await makeGrpcCall('searchTaskRun',
            req,
            res,
            SearchTaskRunRequest.fromJSON({
                statusAndTaskDef: {
                    status: 'TASK_SUCCESS',
                    taskDefName
                },
                limit: 99
            }))
        out = out.concat(successfulTasks.results.filter(r => r.wfRunId === wfRunId) )


        res.send(out)
    }
}
