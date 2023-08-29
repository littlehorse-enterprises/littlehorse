import React, { useEffect, useState } from 'react'
import Image from 'next/image'
import externalEventSvg from './external-event.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'
import { parseKey } from './drawerInternals'
import moment from 'moment'

interface ExternalEventInformationProps {
	isWFRun:boolean
	run?: any
	data: any
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
	console.log('PPD',props.data)
	const onParseError = (data: any) => {

		if (typeof data  == 'string') {
			props.setToggleSideBar(true, true, data, 'str')
			return;
		}
		const key = parseKey(data.type.toLowerCase());
		const error = data[key];
		props.setToggleSideBar(true, true, error, key)
	}
	const parseliteralValue = (v:any) => {
		return 'Literal Value'
		if(v.type === 'BOOL') return  v.bool.toString()
	}
	const parsenodeOutput = (n:any) => {
		return  n.jsonpath
	}


	const [node, setNode] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId:props.wfRunId,
				threadRunNumber: props.run?.number || 0,
                name:props.data?.name?.split('-')[0] || 0
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setNode(result)
			console.log('NODE',result)
		}
    }
	useEffect( () => {
        if(props.isWFRun) getNodeRun()
    },[props.isWFRun])

	return (
		<>
			<div className='component-header'>
				<Image
					src={externalEventSvg}
					alt={'external-event'}
					width={24}
					height={24}
				/>
				<div>
					<p>ExternalEvent Node Information</p>
					<p className='component-header__subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.isWFRun ? (
				<>

					<NodeData reachTime={node?.arrivalTime} completionTime={node?.endTime} status={node?.status}/>
					<div className='drawer__externalEvent__table'>
						<div className='drawer__externalEvent__table__header'>
							ExternalEvent info
						</div>
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
					</div>
					{/* <div className='lh-border lh-round flex flex-col overflow-y-auto'>
						<div className='p-3 text-center lh-drawer-title'>Content</div>
						<div className='grid grid-cols-3'>
							<p className='text-center p-3 lh-node-data-header'>TYPE</p>
							<p className='text-center p-3 col-span-2 lh-node-data'>
								{props.wfRunData.content?.type || ''}
							</p>
							<p className='text-center p-3 lh-node-data-header-single'>
								VALUE
							</p>
							<p className='text-center p-3 col-span-2'>
								{props.wfRunData.content?.value || ''}
							</p>
						</div>
					</div> */}
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
										<p className='center'>{literalValue ? parseliteralValue(literalValue) : parsenodeOutput(nodeOutput) }</p>
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
