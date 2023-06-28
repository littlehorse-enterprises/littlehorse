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
	wfRunDrawer: boolean
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
			<div className='flex justify-center gap-3 m-3'>
				<Image
					src={externalEventSvg}
					alt={'external-event'}
					width={24}
					height={24}
				/>
				<div>
					<p>ExternalEvent Node Information</p>
					<p className='lh-subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunDrawer ? (
				<>
					<NodeData {...props.wfRunData!.nodeData} />
					{props.wfRunData && (
						<>
							<div className='lh-border lh-round flex flex-col overflow-y-auto'>
								<div className='p-3 text-center lh-drawer-title'>
									ExternalEvent info
								</div>
								<div className='grid grid-cols-3'>
									<p className='text-center p-3 lh-node-data-header'>GUID</p>
									<p className='text-center p-3 col-span-2 lh-node-data'>
										{props.wfRunData.guid}
									</p>
									<p className='text-center p-3 lh-node-data-header'>
										ARRIVED TIME
									</p>
									<p className='text-center p-3 col-span-2 lh-node-data'>
										{props.wfRunData.arrivedTime}
									</p>
									<p className='text-center p-3 lh-node-data-header-single'>
										ARRIVED
									</p>
									<p className='text-center p-3 col-span-2'>
										{props.wfRunData.arrived}
									</p>
								</div>
							</div>
							<div className='lh-border lh-round flex flex-col overflow-y-auto'>
								<div className='p-3 text-center lh-drawer-title'>
									Content 💡💡💡
								</div>
								<div className='grid grid-cols-3'>
									<p className='text-center p-3 lh-node-data-header'>TYPE</p>
									<p className='text-center p-3 col-span-2 lh-node-data'>
										{props.wfRunData.content.type}
									</p>
									<p className='text-center p-3 lh-node-data-header-single'>
										VALUE
									</p>
									<p className='text-center p-3 col-span-2'>
										{props.wfRunData.content.value}
									</p>
								</div>
							</div>
						</>
					)}
				</>
			) : (
				<div className='flex flex-col overflow-y-auto lh-border lh-round'>
					<div className='lh-drawer-title text-center p-3'>
						Variables Mutations
					</div>
					<div className='lh-drawer-headers grid grid-cols-3 items-center'>
						<p className='text-center p-3'>MUTATED VARIABLE</p>
						<p className='text-center p-3'>MUTATION TYPE</p>
						<p className='text-center p-3'>
							RHS
							<br />
							(LITERAL VALUE OR VARIABLE)
						</p>
					</div>
					<div className='overflow-y-auto'>
						{props.data &&
							props.data.map(
								(
									{ mutatedVariable, mutatedType, literalValue },
									index: number
								) => {
									return (
										<div
											key={index}
											className='grid grid-cols-3 overflow-x-hidden items-center'
										>
											<p className='text-center p-3'>{mutatedVariable}</p>
											<p className='text-center p-3'>{mutatedType}</p>
											<p className='text-center p-3'>{literalValue}</p>
										</div>
									)
								}
							)}
					</div>
				</div>
			)}
			<FailureInformation data={props.errorData} />
		</>
	)
}
