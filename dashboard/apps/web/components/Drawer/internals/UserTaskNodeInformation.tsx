import moment from 'moment'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import Image from 'next/image'
import { DrawerHeader, DrawerSection } from 'ui'
import _ from 'lodash'
import linkSvg from './link.svg'

interface UserTaskNodeInformationProps {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
    setToggleSideBar: (value:boolean) => void
    setCode:(value:any) => void
}

export function UserTaskNodeInformation({ isWFRun, data, wfRunId, run, setToggleSideBar, setCode }:UserTaskNodeInformationProps) {

    const [ info, setInfo ] = useState<any>()
    const [ node, setNode ] = useState<any>()
    const [ userTaskRun, setUserTaskRun ] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
            method: 'POST',
            body: JSON.stringify({
                wfRunId,
                threadRunNumber: run?.number || 0,
                name:data?.positionInThreadRun
            })
        })
        if (res.ok) {
            res.json().then((result) => {
                setNode(result)
            })
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
            res.json().then((result) => {
                setUserTaskRun(result)
            })
        }
    }
    const getInfo = async () => {

        const res = await fetch('/api/information/userTaskDef', {
            method: 'POST',
            body: JSON.stringify({
                id:data?.lhNode?.userTask?.userTaskDefName,
                version:data?.lhNode?.userTask?.userTaskDefVersion
            })
        })
        if (res.ok) {
            const { result } = await res.json()
            setInfo(result)
        }

    }
    useEffect( () => {
        if (isWFRun) {getNodeRun()}
    },[ isWFRun, data ])

    useEffect( () => {
        getUserTaskRun()
    },[ node ])

    useEffect( () => {
        getInfo()
    },[ data ])
    return (
        <>

            <DrawerHeader image="USER_TASK" name={data.id} title="UserTaskDef Node Information" />

            {isWFRun ? (
                <div className=''>

                    <DrawerSection title="Node Data" >
                        <div className="grid-3">
                            {userTaskRun?.scheduledTime ? <p className="drawer__nodeData__header">SCHEDULED</p> : null}
                            {userTaskRun?.scheduledTime ? <p className="drawer__nodeData__data">{userTaskRun?.scheduledTime ? moment(userTaskRun?.scheduledTime).format('MMMM DD, HH:mm:ss') : ''}</p> : null}
                            <p className="drawer__nodeData__header">REACH TIME</p>
                            <p className="drawer__nodeData__data">{node?.arrivalTime ? moment(node.arrivalTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                            <p className="drawer__nodeData__header">COMPLETION TIME</p>
                            <p className="drawer__nodeData__data">{node?.endTime ? moment(node.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
                            <p className="drawer__nodeData__header">STATUS</p>
                            <p className="drawer__nodeData__data">{node?.status}</p>

                            {(userTaskRun?.userGroup || userTaskRun?.user?.userGroup) ? <p className="drawer__nodeData__header">USER GROUP</p> : null}
                            {(userTaskRun?.userGroup || userTaskRun?.user?.userGroup) ? <p className="drawer__nodeData__data">{userTaskRun?.user?.userGroup?.id || userTaskRun?.userGroup?.id }</p> : null}


                            {userTaskRun?.user ? <p className="drawer__nodeData__header">SPECIFIC USER</p> : null}
                            {userTaskRun?.user ? <p className="drawer__nodeData__data">{userTaskRun?.user?.id }</p> : null}

                            {_.orderBy(userTaskRun?.events, [ 'time' ],[ 'desc' ])?.find( e => e.reassigned?.oldUser) ? <p className="drawer__nodeData__header">REASSIGNED TO</p> : null}
                            {_.orderBy(userTaskRun?.events, [ 'time' ],[ 'desc' ])?.find( e => e.reassigned?.oldUser) ? <p className="drawer__nodeData__data">{_.orderBy(userTaskRun?.events, [ 'time' ],[ 'desc' ])?.find( e => e.reassigned).reassigned.oldUser?.id} {`->`} {_.orderBy(userTaskRun?.events, [ 'time' ],[ 'desc' ])?.find( e => e.reassigned).reassigned.newUser?.id}</p> : null}


                            {userTaskRun?.notes ? <p className="drawer__nodeData__header">NOTES</p> : null}
                            {userTaskRun?.notes ? <p className="drawer__nodeData__data">{userTaskRun?.notes }</p> : null}
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
                                { // TODO: Put logic to make the other type of forms, where we have fields, to work correctly
                                    <tr>
                                        <td>isApproved</td>
                                        <td>{userTaskRun?.results?.isApproved?.type}</td>
                                        <td>{`${userTaskRun?.results?.isApproved?.bool}`}</td>
                                    </tr>
                                }
                            </tbody>
                        </table>
                    </DrawerSection>

                    <DrawerSection title="UserTaskDef Link" >
                        <Link
                            className="drawer-link"
                            href={
                                `/usertaskdef/${data?.lhNode?.userTask?.userTaskDefName}/${data?.lhNode?.userTask?.userTaskDefVersion}`
                            }
                        >
                            <Image alt="link" height={10} src={linkSvg} width={20} />
                            <p className="drawer__task__link__container__clickable__text">
                                {data?.id?.split('-').slice(1, -1).join('-') || ''}
                            </p>
                        </Link>
                    </DrawerSection>

                    <DrawerSection title="Audit Event log" >
                        <div className="drawer-link" onClick={()=> {
                            setToggleSideBar(true)
                            setCode(userTaskRun?.events || '')
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
                            {/* eslint-disable-next-line react/no-array-index-key -- using name + index */}
                            {info?.fields?.map((f, index: number) => <tr key={f.name + index}>
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
