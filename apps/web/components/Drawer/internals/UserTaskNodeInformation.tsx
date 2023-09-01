import moment from "moment"
import { useEffect, useState } from "react"
import { FailureInformation } from "./FailureInformation"
import Link from "next/link"
import Image from "next/image"
import linkSvg from "./link.svg";

interface Props {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
}
export const UserTaskNodeInformation = ({isWFRun, data, wfRunId, run}:Props) => {

    const [info, setInfo] = useState<any>()
    const [node, setNode] = useState<any>()
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
        getInfo()
    },[data])
    return (
        <>
        <div className='component-header'>
            <img src={`/SLEEP.svg`} alt="sleep" />
            <div>
                <p>UserTaskDef Node Information</p>
                <p className='component-header__subheader'>{data?.name && data.name.split('-').slice(0,-1).join('-')}</p>
            </div>
        </div>
        {isWFRun ? (
            <div className=''>
                <div className="drawer__nodeData">
      <div className="drawer__nodeData__label">Node data</div>
      <div className="grid-3">
        {node?.scheduled &&  <p className="drawer__nodeData__header">SCHEDULED</p>}
        {node?.scheduled &&  <p className="drawer__nodeData__data">{node?.scheduled ? moment(node?.scheduled).format('MMMM DD, HH:mm:ss') : ''}</p>}
        <p className="drawer__nodeData__header">ARRIVAL TIME</p>
        <p className="drawer__nodeData__data">{node?.arrivalTime ? moment(node.arrivalTime).format('MMMM DD, HH:mm:ss') : ''}</p>
        <p className="drawer__nodeData__header">END TIME</p>
        <p className="drawer__nodeData__data">{node?.endTime ? moment(node.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
        <p className="drawer__nodeData__header">STATUS</p>
        <p className="drawer__nodeData__data">{node?.status}</p>
        <p className="drawer__nodeData__header">wfRunId</p>
        <p className="drawer__nodeData__data">{node?.userTask?.userTaskRunId?.wfRunId}</p>
        <p className="drawer__nodeData__header">GUID</p>
        <p className="drawer__nodeData__data">{node?.userTask?.userTaskRunId?.userTaskGuid}</p>


      </div>
      
    </div>
    <div style={{ height:'10px'}}></div>


                {/* <div className='drawer__task__wfrun-outputs'>
                    <div className='drawer__task__wfrun-outputs__label'>
                        Form Results
                    </div>
                    <div className='drawer__waitChild__link__container'>
                        <div className='simpleValue__container' >
                            <p className='simpleValue'>
                               
                            </p>
                        </div>
                    </div>
                </div>
                <div style={{ height:'10px'}}></div> */}


                <div className="drawer__task__link">
        <div className="drawer__task__link__title">UserTaskDef link</div>
        <div className="drawer__task__link__container">
          <Link
            href={
              "/usertaskdef/" + data?.node?.userTask?.userTaskDefName + '/' + data?.node?.userTask?.userTaskDefVersion
            }
            className="drawer__task__link__container__clickable"
            style={{
              textDecoration: "none",
            }}
          >
            <Image src={linkSvg} alt={"link"} width={20} height={10} />
            <p className="drawer__task__link__container__clickable__text">
              {data?.name?.split("-").slice(1, -1).join("-") || ""}
            </p>
          </Link>
        </div>
      </div>

      <div style={{ height:'10px'}}></div>
                {/* <div className='drawer__task__wfrun-outputs'>
                    <div className='drawer__task__wfrun-outputs__label'>
                        Audit event log
                    </div>
                    <div className='drawer__waitChild__link__container'>
                        <div className='simpleValue__container' >
                            <p className='simpleValue'>
                               
                            </p>
                        </div>
                    </div>
                </div> */}
            </div>
        ) : (
            <div className='drawer__waitChild__link '>
                <div className="drawer__task__wfrun-outputs">
                    <div className="drawer__task__wfrun-outputs__label">UserTaskDef Fields</div>
                    <div className="drawer__task__wfrun-outputs__header grid-3">
                        <p className="center">NAME</p>
                        <p className="center">DISPLAY NAME</p>
                        <p className="center">TYPE</p>
                    </div>
                    <table>
                        <tbody>
                    {info?.fields?.map((f, index: number) => <tr key={index} className="grid-3">
                            <td className="center">{f.name}</td>
                            <td className="center">{f.displayName}</td>
                            <td className="center">{f.type}</td>
                        </tr>
                    )}
                    </tbody>
                    </table>
                </div>
            </div>
        )}
        {/* <FailureInformation data={errorData} openError={onParseError} /> */}
        {/* data.node.failureHandlers */}
        </>
    )
}