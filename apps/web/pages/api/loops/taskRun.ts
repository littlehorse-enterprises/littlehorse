import { NextApiRequest, NextApiResponse } from "next";

export default async function handler(req:NextApiRequest, res:NextApiResponse) {
    // - TASK_SCHEDULED    
    // - TASK_RUNNING
    // - TASK_SUCCESS
    // - TASK_FAILED
    // - TASK_TIMEOUT
    // - TASK_OUTPUT_SERIALIZING_ERROR
    // - TASK_INPUT_VAR_SUB_ERROR"
    const body = JSON.parse(req.body);
    const { taskDefName, wfRunId } = body;
    console.log(wfRunId)
    let out = []
    let content:any
    if(req.method === 'POST'){
        const TASK_SCHEDULED = await fetch(process.env.API_URL+"/search/taskRun",{
            method:'POST',
            body: JSON.stringify({
                "statusAndTaskDef":{
                    status:"TASK_SCHEDULED",  
                    taskDefName
                },
                "limit":99
            }),
            mode: 'cors',
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
                'Accept': '*/*',
            }
        })
        if(TASK_SCHEDULED.ok){
            content = await TASK_SCHEDULED.json();
            out = out.concat(content.results.filter(r => r.wfRunId ===wfRunId))
        }

        const TASK_RUNNING = await fetch(process.env.API_URL+"/search/taskRun",{
            method:'POST',
            body: JSON.stringify({
                "statusAndTaskDef":{
                    status:"TASK_RUNNING",  
                    taskDefName
                },
                "limit":99
            }),
            mode: 'cors',
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
                'Accept': '*/*',
            }
        })
        if(TASK_RUNNING.ok){
            content = await TASK_RUNNING.json();
            out = out.concat(content.results.filter(r => r.wfRunId ===wfRunId))
        }

        const TASK_SUCCESS = await fetch(process.env.API_URL+"/search/taskRun",{
            method:'POST',
            body: JSON.stringify({
                "statusAndTaskDef":{
                    status:"TASK_SUCCESS",  
                    taskDefName
                },
                "limit":99
            }),
            mode: 'cors',
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
                'Accept': '*/*',
            }
        })
        if(TASK_SUCCESS.ok){
            content = await TASK_SUCCESS.json();
            out = out.concat(content.results.filter(r => r.wfRunId ===wfRunId))
        }

        console.log(out)
        return res.send(out)
        res.send({
            error: "Something goes wrong.",
        })
    }
    



}