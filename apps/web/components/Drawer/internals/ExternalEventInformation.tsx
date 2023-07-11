import React from 'react'
import Image from 'next/image'
import externalEventSvg from './external-event.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'

interface ExternalEventInformationProps {
	data: { mutatedVariable: string; mutatedType: string; literalValue: string }[]
	nodeName: any
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	wfRunData?: {
		nodeData: NodeDataProps
		guid: string
		arrivedTime: string
		arrived: string
		content: {
			type: string
			value: string
		}
	}
}

export const ExternalEventInformation = (
	props: ExternalEventInformationProps
) => {
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
			{props.wfRunData ? (
				<>
					<NodeData {...props.wfRunData.nodeData} />
					<div className='drawer__externalEvent__table'>
						<div className='drawer__externalEvent__table__header'>
							ExternalEvent info
						</div>
						<div className='grid-3'>
							<p className='drawer__nodeData__header'>GUID</p>
							<p className='drawer__nodeData__data'>{props.wfRunData.guid}</p>
							<p className='drawer__nodeData__header'>ARRIVED TIME</p>
							<p className='drawer__nodeData__data'>
								{props.wfRunData.arrivedTime}
							</p>
							<p className='drawer__nodeData__headerSimple'>ARRIVED</p>
							<p className='drawer__nodeData__dataSimple'>
								{props.wfRunData.arrived}
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
						props.data.map(
							(
								{ mutatedVariable, mutatedType, literalValue },
								index: number
							) => {
								return (
									<div key={index} className='grid-3'>
										<p className='center'>{mutatedVariable}</p>
										<p className='center'>{mutatedType}</p>
										<p className='center'>{literalValue}</p>
									</div>
								)
							}
						)}
				</div>
			)}
			<FailureInformation data={props.errorData} />
		</>
	)
}
