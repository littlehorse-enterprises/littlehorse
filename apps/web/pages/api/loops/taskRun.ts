import type { NextApiRequest, NextApiResponse } from 'next'
import { createChannel, createClient } from 'nice-grpc'
import type { Client } from 'nice-grpc/src/client/Client'
import { LHPublicApiDefinition, SearchTaskRunRequest } from '../../../littlehorse-public-api/service'
import LHClient from '../LHClient'

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    if (process.env.API_URL === undefined) {
        res.send({
            error: 'Not API URL provided in the configuration.',
        })
    } else {
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

        if (req.method === 'POST'){
            try {
                const client: Client<LHPublicApiDefinition> = LHClient.getInstance()

                const scheduledTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef':{
                        status:'TASK_SCHEDULED',  
                        taskDefName
                    },
                    'limit':99
                }) as any)
                out = out.concat(scheduledTasks.results.filter(r => r.wfRunId ===wfRunId) as any)
            } catch (error) {
                console.error('TaskRun - Error during GRPC call:', error)
            }

            try {
                const channel = createChannel(process.env.API_URL)
                const client = createClient(LHPublicApiDefinition, channel)

                const runningTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef':{
                        status:'TASK_RUNNING',  
                        taskDefName
                    },
                    'limit':99
                }) as any)
                out = out.concat(runningTasks.results.filter(r => r.wfRunId ===wfRunId) as any)
            } catch (error) {
                console.error('loops/taskRun - Error during GRPC call:', error)
            }

            try {
                const channel = createChannel(process.env.API_URL)
                const client = createClient(LHPublicApiDefinition, channel)

                const successfulTasks = await client.searchTaskRun(SearchTaskRunRequest.fromJSON({
                    'statusAndTaskDef':{
                        status:'TASK_SUCCESS',  
                        taskDefName
                    },
                    'limit':99
                }) as any)
                out = out.concat(successfulTasks.results.filter(r => r.wfRunId ===wfRunId) as any)
            } catch (error) {
                console.error('Error during GRPC call:', error)
            }

            res.send(out) 
        }
    }
}
