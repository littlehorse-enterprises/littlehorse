import Link from "next/link"

export const Drawer = ({output, thread, data}:any) => {
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

        {output.type == 'TASK' ? <div>
            <h4>Task Node Information</h4>
            <div style={{marginBottom:'20px'}}>{output.name}</div>
            <div className="table" style={{marginBottom:'20px'}}>
                <table>
                    <caption>TaskDef Variables</caption>
                </table>
            </div>
            <div className="table">
                <table>
                    <caption>TaskDef linked</caption>
                    <tbody><tr><td><Link href={'/taskdef/'+output.node.task.taskDefName}>{output.node.task.taskDefName}</Link></td></tr></tbody>
                </table>
            </div>

            
        </div> : undefined}
        <div className="frame" style={{overflow:"auto"}}>

            {JSON.stringify(output.node, null,2)}
            {/* {JSON.stringify(data, null,2)} */}
            {/* {JSON.stringify(output, null,2)} */}
        </div>
        <div className="flex-1"></div>
    </div>
}