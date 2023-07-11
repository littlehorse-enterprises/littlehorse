import React from 'react'
import Image from 'next/image'
import splitArrowSvg from './split-arrow.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import {
	conditionSymbol,
	getFullVariableName,
	getNOP_RHS
} from './drawerInternals'

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
					<div className='component-header'>
						<Image
							src={splitArrowSvg}
							alt={'split-arrow'}
							width={24}
							height={24}
						/>
						<div>
							<p>NOP Information</p>
							<p className='component-header__subheader'>{props.nodeName}</p>
						</div>
					</div>
					<div className='drawer__nop__table'>
						<div className='drawer__nop__table__header '>Node Conditions</div>
						<div className='drawer__nop__table__header__subheaders'>
							<p className='center'>LHS</p>
							<p className='center'>Condition</p>
							<p className='center'>RHS</p>
							<p className='center'>SINK NODE NAME</p>
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
										<div key={index} className='grid-4'>
											<p className='center'>
												{getFullVariableName(element.condition?.left)}
											</p>
											<p className='center'>
												{conditionSymbol(element.condition?.comparator)}
											</p>
											<p className='center'>
												{getNOP_RHS(element.condition?.right.literalValue)}
											</p>
											<p className='center'>{element.sinkNodeName}</p>
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
