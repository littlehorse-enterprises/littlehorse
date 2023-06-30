"use client"
import Link from "next/link"
import { useEffect, useState } from "react";

export const Drawer = ({output, thread, data}:any) => {
    const [taskdef_variables, setTaskDefVariables] = useState<any[]>([])
    const taskInformation = async (id:string) => {

        const res = await fetch('/api/information/taskDef',{
            method:'POST',
            body: JSON.stringify({
                id
            }),
        })
        if(res.ok){
            const data: any = await res.json();
            // const data: TaskDefInformationResponse = await res.json();
            console.log(data)
            setTaskDefVariables(data.result.inputVars);
            // setLoadingInputVars(false);
        }
    }

    useEffect(() => {
        if(output.type == 'TASK') taskInformation(output.node.task.taskDefName)
    },[output])

    return <div className="drawer">

        <h2 style={{
            paddingBottom:'8px',
            paddingTop:'8px',
            fontSize:"20px"
        }} >WfSpec Properties</h2>
        <div className="frame">
            <label>THREADSPEC</label>
            <div className="input">{data?.entrypointThreadName}</div>

            {output ? <div >
                <label>TYPE</label>
                <div className="input">{output.type}</div>
            </div> : undefined}

        </div>

        <div className="table">
            <table>
                <caption>ThreadSpec Variables</caption>
                <thead>
                    <tr>
                        <th>NAME</th>
                        <th>TYPE</th>
                    </tr>
                </thead>
                <tbody>
                    {data?.threadSpecs[data?.entrypointThreadName]?.variableDefs?.map( (v, i) => <tr key={i}>
                        <td>{v.name}</td>
                        <td>{v.type}</td>
                        {/* <td>{v.required}</td> */}
                    </tr>)}

                </tbody>
            </table>
            
        </div>

        {output.type == 'TASK' ? <div style={{marginBottom:'20px'}}>
                <div className="card" style={{marginBottom:'20px'}}>
                    <img src="/TASKICON.svg" />
                    <div className="text">
                    <h4>Task Node Information</h4>
                    <div >{output.name}</div>
                </div>
            </div>

            <div className="table" style={{marginBottom:'20px'}}>
                <table>
                    <caption>TaskDef Variables</caption>
                    <thead>
                    <tr>
                        <th>NAME</th>
                        <th>TYPE</th>
                    </tr>
                </thead>
                <tbody>
                    {taskdef_variables?.map( (v, i) => <tr key={i}>
                        <td>{v.name}</td>
                        <td>{v.type}</td>
                        {/* <td>{v.required}</td> */}
                    </tr>)}

                </tbody>
                </table>
            </div>
            <div className="table">
                <table>
                    <caption>TaskDef linked</caption>
                    <tbody><tr><td><Link href={'/taskdef/'+output.node.task.taskDefName}>{output.node.task.taskDefName}</Link></td></tr></tbody>
                </table>
            </div>

            
        </div> : undefined}
        {/* <div className="frame" style={{overflow:"auto"}}>
            {JSON.stringify(data, null,2)}
            {JSON.stringify(output, null,2)}
        </div> */}
        <div className="flex-1"></div>
    </div>
}