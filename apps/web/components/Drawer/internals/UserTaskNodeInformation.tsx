import moment from "moment"
import { useEffect, useState } from "react"
import { FailureInformation } from "./FailureInformation"
import Link from "next/link"
import Image from "next/image"
import linkSvg from "./link.svg";
import { DrawerSection } from "ui"
import { orderBy } from "lodash"

interface Props {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
    setToggleSideBar: (value:boolean) => void
    setCode:(value:any) => void
}
const parseValueByType = (value:any) => {
    if(value?.type === 'JSON_OBJ') return JSON.stringify(value?.jsonObj)
    if(value?.type === 'JSON_ARR') return JSON.stringify(value?.jsonArr)
    if(value?.type === 'DOUBLE') return JSON.stringify(value?.double)
    if(value?.type === 'BOOL') return JSON.stringify(value?.bool)
    if(value?.type === 'INT') return (value?.int)
    if(value?.type === 'BYTES') return (value?.bytes)
    return value?.str
}
export const UserTaskNodeInformation = ({isWFRun, data, wfRunId, run, setToggleSideBar, setCode}:Props) => {

    const [info, setInfo] = useState<any>()
    const [node, setNode] = useState<any>()
    const [nrun, setRun] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId,
				threadRunNumber: run?.number || 0,
                name:data?.name?.split('-')[0] || 0
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setNode(result)
		}
    }
    const getUserTaskRun = async () => {
        const res = await fetch('/api/drawer/userTaskRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId,
				guid:node?.userTask?.userTaskRunId?.userTaskGuid
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setRun(result)
		}
    }
    const getInfo = async () => {

        const res = await fetch('/api/information/userTaskDef', {
			method: 'POST',
			body: JSON.stringify({
				id:data?.node?.userTask?.userTaskDefName,
				version:data?.node?.userTask?.userTaskDefVersion
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setInfo(result)
		}
       
    }
    useEffect( () => {
        if(isWFRun) getNodeRun()
    },[isWFRun, data])

    useEffect( () => {
        getUserTaskRun()
    },[node])

    useEffect( () => {
        getInfo()
    },[data])
    return (
        <>
        <div className='component-header'>
            <img src={`/USER_TASK.svg`} alt="sleep" />
            <div>
                <p>UserTaskDef Node Information</p>
                <p className='component-header__subheader'>{data?.name && data.name.split('-').slice(0,-1).join('-')}</p>
            </div>
        </div>
        {isWFRun ? (
            <div className=''>

            <DrawerSection title="Node Data" >
                <div className="grid-3">
                    {nrun?.scheduledTime &&  <p className="drawer__nodeData__header">SCHEDULED</p>}
                    {nrun?.scheduledTime &&  <p className="drawer__nodeData__data">{nrun?.scheduledTime ? moment(nrun?.scheduledTime).format('MMMM DD, HH:mm:ss') : ''}</p>}
                    <p className="drawer__nodeData__header">ARRIVAL TIME</p>
                    <p className="drawer__nodeData__data">{node?.arrivalTime ? moment(node.arrivalTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                    <p className="drawer__nodeData__header">END TIME</p>
                    <p className="drawer__nodeData__data">{node?.endTime ? moment(node.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                    <p className="drawer__nodeData__header">STATUS</p>
                    <p className="drawer__nodeData__data">{node?.status}</p>

                    {(nrun?.userGroup || nrun?.user?.userGroup) &&  <p className="drawer__nodeData__header">USER GROUP</p>}
                    {(nrun?.userGroup || nrun?.user?.userGroup) &&  <p className="drawer__nodeData__data">{nrun?.user?.userGroup?.id || nrun?.userGroup?.id }</p>}
                    

                    {nrun?.user &&  <p className="drawer__nodeData__header">SPECIFIC USER</p>}
                    {nrun?.user &&  <p className="drawer__nodeData__data">{nrun?.user?.id }</p>}
                    
                    {orderBy(nrun?.events, ["time"],["desc"])?.find( e => e.reassigned?.oldUser) &&  <p className="drawer__nodeData__header">REASSIGNED TO</p>}
                    {orderBy(nrun?.events, ["time"],["desc"])?.find( e => e.reassigned?.oldUser) &&  <p className="drawer__nodeData__data">{orderBy(nrun?.events, ["time"],["desc"])?.find( e => e.reassigned).reassigned.oldUser?.id} {`->`} {orderBy(nrun?.events, ["time"],["desc"])?.find( e => e.reassigned).reassigned.newUser?.id}</p>}

                   
                    {nrun?.notes &&  <p className="drawer__nodeData__header">NOTES</p>}
                    {nrun?.notes &&  <p className="drawer__nodeData__data">{nrun?.notes }</p>}
                </div>
            </DrawerSection>

            <DrawerSection title="Form Results" >
                <table>
                    <thead>
                        <tr>
                            <th>NAME</th>
                            <th>TYPE</th>
                            <th>RESULT</th>
                        </tr>
                    </thead>
                    <tbody>
                        {nrun?.results?.map( (r:any, ix:number) => <tr key={ix}>
                            <td>{r.name}</td>
                            <td>{r.value?.type}</td>
                            <td>{parseValueByType(r.value)}</td>
                        </tr>)}
                    </tbody>
                </table>
            </DrawerSection>
            
            <DrawerSection title="UserTaskDef Link" >
                <Link
                    href={
                    "/usertaskdef/" + data?.node?.userTask?.userTaskDefName + '/' + data?.node?.userTask?.userTaskDefVersion
                    }
                    className="drawer-link"
                >
                    <Image src={linkSvg} alt={"link"} width={20} height={10} />
                    <p className="drawer__task__link__container__clickable__text">
                    {data?.name?.split("-").slice(1, -1).join("-") || ""}
                    </p>
                </Link>
            </DrawerSection>

            <DrawerSection title="Audit Event log" >
                <div className="drawer-link" onClick={()=> {
                                    setToggleSideBar(true)
                                    setCode(nrun?.events || "")
                            }}>
                          audit log      
                </div>
            </DrawerSection>

            </div>
        ) : (

            <DrawerSection title="UserTaskDef Fields" >
                <table>
                    <thead>
                        <tr>
                            <th>NAME</th>
                            <th>DISPLAY NAME</th>
                            <th>TYPE</th>
                        </tr>
                    </thead>
                    <tbody>
                     {info?.fields?.map((f, index: number) => <tr key={index}>
                            <td>{f.name}</td>
                            <td>{f.displayName}</td>
                            <td>{f.type}</td>
                        </tr>
                    )}
                </tbody>
                </table>
            </DrawerSection>
        )}
        {/* <FailureInformation data={errorData} openError={onParseError} /> */}
        {/* data.node.failureHandlers */}
        </>
    )
}