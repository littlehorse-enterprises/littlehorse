import moment from "moment"
import { useEffect, useState } from "react"
import { FailureInformation } from "./FailureInformation"
import Link from "next/link"
import Image from "next/image"
import linkSvg from "./link.svg";
import { DrawerHeader, DrawerSection } from "ui"
import { orderBy } from "lodash"
import { parseValueByType } from "../../../helpers/parseValueByType"

interface Props {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
    setToggleSideBar: (value:boolean) => void
    setCode:(code:any) => void
}

export const TaskInformation = ({isWFRun, data, wfRunId, run, setToggleSideBar, setCode}:Props) => {

    const [attempt_no, setAttemptNo] = useState(0)
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
      if(!node?.task?.taskRunId) return 
        const res = await fetch('/api/drawer/taskRun', {
			method: 'POST',
			body: JSON.stringify(node?.task?.taskRunId)
		})
        if (res.ok) {
			const {result} = await res.json()
            setRun(result)
            // if(result.attempts.length)setAttempt(result.attempts[0])
		}
    }
    const getInfo = async () => {

        const res = await fetch('/api/information/taskDef', {
			method: 'POST',
			body: JSON.stringify({
				id:data?.node?.task?.taskDefName,
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

        <DrawerHeader name={data?.name} title="Task Node Information" image="TASK" />
        
        {isWFRun ? (
            <div className=''>
            <DrawerSection title="TaskDef Variables" >
                <table>
                    <thead>
                        <tr>
                            <th>NAME</th>
                            <th>TYPE</th>
                            <th>VALUE</th>
                        </tr>
                    </thead>
                    <tbody>
                        {nrun?.inputVariables?.map( (r:any, ix:number) => <tr key={ix}>
                            <td>{r.varName}</td>
                            <td>{r.value?.type}</td>
                            <td>{parseValueByType(r.value)}</td>
                        </tr>)}
                    </tbody>
                </table>
            </DrawerSection>
            {/* <pre>{JSON.stringify(nrun?.attempts, null, 2)}</pre> */}
            <div className="task_attempts">
              <div className="title">Task Attempt</div>
              <div className="selector">
                <div className="icon">
                  <img src="/replay.svg" />
                </div>
                <div className='version_select'>
                  <select  value={attempt_no} onChange={ e => setAttemptNo(+e.target.value)}>
                      {nrun?.attempts?.map( (_, ix:number) => <option key={ix} value={ix}> {nrun?.taskDefName} [{ix}]</option>)}
                  </select>
                  <img style={{marginLeft:"30px"}} src="/expand_more.svg" />
                </div>
              </div>

            </div>

            {nrun?.attempts.length && <DrawerSection title="Node Data" >
              <div className="grid-3">
                {nrun?.attempts?.[attempt_no || 0]?.scheduleTime &&  <p className="drawer__nodeData__header">SCHEDULED</p>}
                {nrun?.attempts?.[attempt_no || 0]?.scheduleTime &&  <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.scheduleTime ? moment(nrun?.attempts?.[attempt_no || 0]?.scheduleTime).format('MMMM DD, HH:mm:ss') : ''}</p>}
                <p className="drawer__nodeData__header">REACH TIME</p>
                <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.startTime ? moment(nrun?.attempts?.[attempt_no || 0]?.startTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                <p className="drawer__nodeData__header">COMPLETION TIME</p>
                <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.endTime ? moment(nrun?.attempts?.[attempt_no || 0]?.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                <p className="drawer__nodeData__header">TASK WORKER ID</p>
                <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.taskWorkerId || "-----"}</p>
                <p className="drawer__nodeData__header">STATUS</p>
                <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.status}</p>
              </div>
            </DrawerSection>}
            {/* <pre>{JSON.stringify(node, null,2)}</pre> */}
            {/* <pre>{JSON.stringify(nrun?.attempts?.[attempt_no || 0], null,2)}</pre> */}
            {/* <pre>{JSON.stringify(info, null,2)}</pre> */}
            {nrun?.attempts?.length &&<DrawerSection title="Outputs" >
                <table>
                    <thead>
                        <tr>
                            <th>TYPE</th>
                            <th>VALUE</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>{nrun?.attempts?.[attempt_no || 0]?.output?.type}</td>
                            <td>{parseValueByType(nrun?.attempts?.[attempt_no || 0]?.output)}</td>
                        </tr>
                    </tbody>
                </table>
            </DrawerSection>}
            


            </div>
        ) : (

            <DrawerSection title="TaskDef Variables" >
                <table>
                    <thead>
                        <tr>
                            <th>NAME</th>
                            <th>TYPE</th>
                        </tr>
                    </thead>
                    <tbody>
                     {info?.inputVars?.map((f, index: number) => <tr key={index}>
                            <td>{f.name}</td>
                            <td>{f.type}</td>
                        </tr>
                    )}
                </tbody>
                </table>
            </DrawerSection>
        )}

        <DrawerSection title="TaskDef Link" >
                <Link
                    href={
                    "/taskdef/" + data?.node?.task?.taskDefName
                    }
                    className="drawer-link"
                >
                    <Image src={linkSvg} alt={"link"} width={20} height={10} />
                    <p className="drawer__task__link__container__clickable__text">
                    {data?.node?.task?.taskDefName || ""}
                    </p>
                </Link>
            </DrawerSection>
        {/* <FailureInformation data={errorData} openError={onParseError} /> */}
        {/* data.node.failureHandlers */}
        </>
    )
}