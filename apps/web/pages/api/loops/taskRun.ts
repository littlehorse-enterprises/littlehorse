import type { NextApiRequest, NextApiResponse } from 'next'
import type { Client } from 'nice-grpc/src/client/Client'
import type { LHPublicApiDefinition } from '../../../littlehorse-public-api/service'
import { SearchTaskRunRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'
import { getServerSession } from 'next-auth/next'
import { authOptions } from '../auth/[...nextauth]'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    if (req.method === 'POST') {
        const session = await getServerSession(req, res, authOptions)
        
        if (session) {
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

            const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

            try {
                const scheduledTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef': {
                        status: 'TASK_SCHEDULED',
                        taskDefName
                    },
                    'limit': 99
                }) as any)
                out = out.concat(scheduledTasks.results.filter(r => r.wfRunId === wfRunId) as any)
            } catch (error) {
                console.error('TaskRun - Error during GRPC call:', error)
            }

            try {
                const runningTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef': {
                        status: 'TASK_RUNNING',
                        taskDefName
                    },
                    'limit': 99
                }) as any)
                out = out.concat(runningTasks.results.filter(r => r.wfRunId === wfRunId) as any)
            } catch (error) {
                console.error('loops/taskRun - Error during GRPC call:', error)
            }

            try {
                const successfulTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef': {
                        status: 'TASK_SUCCESS',
                        taskDefName
                    },
                    'limit': 99
                }) as any)
                out = out.concat(successfulTasks.results.filter(r => r.wfRunId === wfRunId) as any)
            } catch (error) {
                console.error('Error during GRPC call:', error)
            }

            res.send(out)
        } else {
            res.status(401)
                .json({
                    status: 401,
                    message: 'You need to be authenticated to access this resource.'
                })
        }
    }
}
