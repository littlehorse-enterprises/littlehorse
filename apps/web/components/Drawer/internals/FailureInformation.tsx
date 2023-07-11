import React from 'react'
import Image from 'next/image'
import exceptionSvg from './exception.svg'

export enum LH_EXCEPTION {
	CHILD_FAILURE = 'Child failure',
	VAR_SUB_ERROR = 'Variable sub error',
	VAR_MUTATION_ERROR = 'Variable mutation error',
	TIMEOUT = 'Timeout',
	TASK_FAILURE = 'Task failure'
}

interface FailureInformationProps {
	data: { handlerSpecName: string; exception: LH_EXCEPTION | string }[]
}

export const FailureInformation = (props: FailureInformationProps) => {
	return (
		<>
			{props.data.length > 0 && (
				<>
					<div className='lh-border lh-round flex flex-col overflow-y-auto'>
						<div className='lh-drawer-title flex gap-3 justify-center p-3'>
							<Image
								src={exceptionSvg}
								alt={'exception'}
								width={24}
								height={24}
							/>
							<p>Failure Handlers</p>
						</div>
						<div className='lh-drawer-headers grid grid-cols-2'>
							<p className='text-center p-3'>EXCEPTION TYPE</p>
							<p className='text-center p-3'>HANDLER THREAD NAME</p>
						</div>
						{props.data.map((element, index: number) => {
							return (
								<div
									key={index}
									className='grid grid-cols-2 overflow-x-hidden items-center'
								>
									<p className='text-center p-3'>{element.exception}</p>
									<p className='text-center p-3'>{element.handlerSpecName}</p>
								</div>
							)
						})}
					</div>
				</>
			)}
		</>
	)
}
