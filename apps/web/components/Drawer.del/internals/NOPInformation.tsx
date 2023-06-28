import React from 'react'
import Image from 'next/image'
import splitArrowSvg from './split-arrow.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { conditionSymbol, getFullVariableName, getNOP_RHS } from '../drawerInternals/drawerInternals'

interface NOPInformationProps {
	data: { sinkNodeName: string; condition: outgoingEdgesCondition }[]
	nodeName: string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
}

interface outgoingEdgesCondition {
	comparator: string
	left: {
		jsonPath: string
		variableName: string
	}
	right: {
		literalValue: any
	}
}

export const NOPInformation = (props: NOPInformationProps) => {
	return (
		<>
			{props.nodeName && (
				<>
					<div className='flex justify-center gap-3 m-3'>
						<Image
							src={splitArrowSvg}
							alt={'split-arrow'}
							width={24}
							height={24}
						/>
						<div>
							<p>NOP Information</p>
							<p className='lh-subheader'>{props.nodeName}</p>
						</div>
					</div>
					<div className='flex flex-col overflow-y-auto lh-border lh-round'>
						<div className='p-3 text-center lh-drawer-title'>
							Node Conditions
						</div>
						<div className='grid grid-cols-4 lh-drawer-headers'>
							<p className='p-3 text-center'>LHS</p>
							<p className='p-3 text-center'>Condition</p>
							<p className='p-3 text-center'>RHS</p>
							<p className='p-3 text-center'>SINK NODE NAME</p>
						</div>
						{props.data &&
							props.data.map(
								(
									element: {
										sinkNodeName: string
										condition: outgoingEdgesCondition
									},
									index: number
								) => {
									return (
										<div
											key={index}
											className='grid items-center grid-cols-4 overflow-x-hidden'
										>
											<p className='p-3 text-center'>
												{getFullVariableName(element.condition?.left)}
											</p>
											<p className='p-3 text-center'>
												{conditionSymbol(element.condition?.comparator)}
											</p>
											<p className='p-3 text-center'>
												{getNOP_RHS(element.condition?.right.literalValue)}
											</p>
											<p className='p-3 text-center'>{element.sinkNodeName}</p>
										</div>
									)
								}
							)}
					</div>
					<FailureInformation data={props.errorData} />
				</>
			)}
		</>
	)
}
