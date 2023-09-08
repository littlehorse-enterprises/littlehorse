import React, { useEffect, useState } from 'react'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { parseKey } from './drawerInternals'
import moment from 'moment'
import { DrawerHeader, DrawerSection } from 'ui'
import { nodeposition } from '../../../helpers/nodename'
import { parseValueByType } from '../../../helpers/parseValueByType'

interface ExternalEventInformationProps {
	isWFRun:boolean
	run?: any
	data?: any
	nodeName: any
	wfRunId?:string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export const ExternalEventInformation = (
	props: ExternalEventInformationProps
) => {

	const onParseError = (data: any) => {

		if (typeof data  == 'string') {
			props.setToggleSideBar(true, true, data, 'str')
			return;
		}
		const key = parseKey(data.type.toLowerCase());
		const error = data[key];
		props.setToggleSideBar(true, true, error, key)
	}

	const [node, setNode] = useState<any>()
	const [nrun, setRun] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId:props.wfRunId,
				threadRunNumber: props.run?.number || 0,
                name:nodeposition(props.data?.name)
			})
		})
        if (res.ok) {
			const {result} = await res.json()
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
        if(props.isWFRun) getNodeRun()
    },[props.isWFRun, props.data])

	return (
		<>	
			<DrawerHeader name={props.data?.name} title="ExternalEvent Node Information" image="EXTERNAL_EVENT" />

			{props.isWFRun ? (
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
					
					{/* <pre>{JSON.stringify(node, null, 2)}</pre> */}

					{/* <pre>{JSON.stringify(nrun, null, 2)}</pre> */}
					<DrawerSection title="ExternalEvent info" >
						<div className='grid-3'>
							<p className='drawer__nodeData__header'>GUID</p>
							<p className='drawer__nodeData__data'>{node?.externalEvent?.externalEventId?.guid}</p>
							<p className='drawer__nodeData__header'>ARRIVED TIME</p>
							<p className='drawer__nodeData__data'>
								{node?.externalEvent?.eventTime ? moment(node.externalEvent?.eventTime).format('MMMM DD, HH:mm:ss') : ''}
							</p>
							<p className='drawer__nodeData__header'>ARRIVED</p>
							<p className='drawer__nodeData__dataSimple'>
								{node?.externalEvent?.eventTime ? 'YES' : 'NO'}
							</p>
						</div>
					</DrawerSection>

					{nrun && <DrawerSection title="Content" >
						<div className='grid-3'>
							<p className='drawer__nodeData__header'>TYPE</p>
							<p className='drawer__nodeData__data'>{nrun?.content?.type}</p>
							{ (nrun?.content?.type != 'NULL') && <p className='drawer__nodeData__header'>VALUE</p>}
							{ (nrun?.content?.type != 'NULL') && <p className='drawer__nodeData__data'>{parseValueByType(nrun?.content)}</p>}
						</div>
					</DrawerSection>}
				</>
			) : (
				<div className='drawer__externalEvent__table'>
					<div className='drawer__externalEvent__table__header'>
						Variables Mutations
					</div>
					<div className='drawer__externalEvent__table__header__subheaders'>
						<p className='center'>MUTATED VARIABLE</p>
						<p className='center'>MUTATION TYPE</p>
						<p className='center'>
							RHS
							<br />
							(LITERAL VALUE OR VARIABLE)
						</p>
					</div>
					{props.data &&
						props.data.node?.variableMutations?.map(
							(
								{ lhsName, operation, literalValue, nodeOutput },
								index: number
							) => {
								return (
									<div key={index} className='grid-3'>
										<p className='center'>{lhsName}</p>
										<p className='center'>{operation}</p>
										<p className='center'>{literalValue ? "Literal Value" : "Variable" }</p>
										{/* <p className='center'>{literalValue ? parseliteralValue(literalValue) : parsenodeOutput(nodeOutput) }</p> */}
									</div>
								)
							}
						)}

				</div>
			)}
			<FailureInformation data={props.errorData} openError={onParseError} />
		</>
	)
}
