import React, { useEffect, useState } from 'react'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { parseKey } from './drawerInternals'
import moment from 'moment'
import { DrawerHeader, DrawerLink, DrawerSection, DrawerThreadSpecLink } from 'ui'
import { nodename, nodeposition } from '../../../helpers/nodename'
import { parseValueByType } from '../../../helpers/parseValueByType'
import Link from 'next/link'

interface Props {
	linkedThread: (thread:string) => void
	isWFRun:boolean
	run?: any
	data?: any
	wfRunId?:string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export const WaitForThreadsInformation = (
	{isWFRun,run,data,wfRunId,errorData,setToggleSideBar, linkedThread}: Props
) => {

	const onParseError = (data: any) => {

		if (typeof data  == 'string') {
			setToggleSideBar(true, true, data, 'str')
			return;
		}
		const key = parseKey(data.type.toLowerCase());
		const error = data[key];
		setToggleSideBar(true, true, error, key)
	}

	const [node, setNode] = useState<any>()
	const [nrun, setRun] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId:wfRunId,
				threadRunNumber: run?.number || 0,
                name:nodeposition(data?.name)
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            console.log('RESULT',result)
            setNode(result)
		}
    }
	const getUserTaskRun = async () => {
        const res = await fetch('/api/drawer/externalEvent', {
			method: 'POST',
			body: JSON.stringify({
				...node?.externalEvent?.externalEventId
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setRun(result)
		}
    }
	useEffect( () => {
        getUserTaskRun()
    },[node])

	useEffect( () => {
        if(isWFRun) getNodeRun()
    },[isWFRun, data])

	return (
		<>	
			<DrawerHeader name={data?.name} title="WaitForThreads Node Information" image="WAIT_FOR_THREADS" />

			{isWFRun ? (
				<>
					<DrawerSection title="Node Data" >
						<div className="grid-3">
							{nrun?.scheduledTime &&  <p className="drawer__nodeData__header">SCHEDULED</p>}
							{nrun?.scheduledTime &&  <p className="drawer__nodeData__data">{nrun?.scheduledTime ? moment(nrun?.scheduledTime).format('MMMM DD, HH:mm:ss') : ''}</p>}
							<p className="drawer__nodeData__header">REACH TIME</p>
							<p className="drawer__nodeData__data">{node?.arrivalTime ? moment(node.arrivalTime).format('MMMM DD, HH:mm:ss') : ''}</p>
							<p className="drawer__nodeData__header">COMPLETION TIME</p>
							<p className="drawer__nodeData__data">{node?.endTime ? moment(node.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
							<p className="drawer__nodeData__header">STATUS</p>
							<p className="drawer__nodeData__data">{node?.status}</p>
						</div>
					</DrawerSection>	
				</>
			) : (
				<>
					{/* <pre>{JSON.stringify(data?.node, null,2)}</pre> */}
				</>
			)}
			<DrawerSection title="Related ThreadSpec" >
				{data?.node?.waitForThreads?.threads?.map( (t:any, ix:number) => <DrawerThreadSpecLink onClick={linkedThread} key={ix} name={nodename(t.threadRunNumber?.variableName)} />)}
			</DrawerSection>
			<FailureInformation data={errorData} openError={onParseError} />
		</>
	)
}
