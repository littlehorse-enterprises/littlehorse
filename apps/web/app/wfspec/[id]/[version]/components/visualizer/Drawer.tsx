
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
        <div className="frame" style={{overflow:"auto"}}>

            {JSON.stringify(data, null,2)}
            {/* {JSON.stringify(output, null,2)} */}
        </div>
        <div className="flex-1"></div>
    </div>
}