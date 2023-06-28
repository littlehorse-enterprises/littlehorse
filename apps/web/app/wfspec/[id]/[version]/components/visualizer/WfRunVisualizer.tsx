"use client"
import { useEffect, useState } from "react"
import { WfSpecVisualizerChart } from "./WfSpecVisualizerChart";
import { Drawer } from "./Drawer";
// import { DrawerComponent } from "../../../../../../components/Drawer/DrawerComponent";
// import { Drawer } from "../../../../../../components/Drawer/Drawer";

interface mapnode{
    
}
export const WfRunVisualizer = ({id, version}:{id:string, version:number}) => {

    const [fulldata, setFullData] = useState<any>()
    const [data, setData] = useState<any[]>([])
    const [output, setOutput] = useState<any>('')
    // const [drawerData, setDrawerData] = useState<any>()
	// const [selectedNodeName, setSelectedNodeName] = useState<any>()
	// const [nodeType, setNodeType] = useState<string | undefined>()

    const rec = (mappedData, i) => {
        let el = mappedData[i]
        if(!el.childs.length) return mappedData
        if(el.type === 'WAIT_FOR_THREAD') {
            let wft = el.node.waitForThread.threadRunNumber.variableName
            let thread = (mappedData.find( m => m.name === wft))
            el.wlevel = thread.level
        }
        mappedData = mappedData.map( m => {
            if(el.childs.includes(m.name)){
                m.level = el.level+1
                if(el.childs.length>1) {
                    m.level = el.level+2
                    m.px = m.name === el.childs[0] ? 'left' : 'right'
                }
            } 
            return m
        })
        return rec(mappedData,++i)
    }
    const mapData = (data:any) => {
        const entries = Object.entries(data?.threadSpecs?.entrypoint?.nodes)
        const mappedData:any = entries.map((e:mapnode) => ({
            name:e[0],
            type:e[0].split("-").pop(),
            node:e[1],
            childs:e[1]["outgoingEdges"].map( e => e.sinkNodeName),
            level:0,
            px:'center'
        }))
        return rec(mappedData,0)
    }
    const getData = async () => {
        const res = await fetch('/api/visualization/wfSpec',{
            method:'POST',
            body: JSON.stringify({
                id, version
            }),
        })
        if(res.ok){
            const content = await res.json()
            setFullData(content.result)
            setData( mapData(content.result))
        }
    }

    // const drawerInternal = (
	// 	<DrawerComponent
	// 		internalComponent={nodeType}
	// 		data={drawerData}
	// 		nodeName={selectedNodeName}
	// 		wfRunDrawer={false}
	// 	/>
	// )

    useEffect( () => {
        getData()
    },[])
    return (
        <div className="visualizer">
            <div className="canvas">
                <WfSpecVisualizerChart data={data} onClick={setOutput} />

            </div>
            
            <Drawer output={output} thread={id} data={fulldata} />
            {/* <Drawer title={'WfSpec Properties'}>{drawerInternal}</Drawer> */}
            {/* {JSON.stringify(output, null,2)} */}

        </div>
    )
}