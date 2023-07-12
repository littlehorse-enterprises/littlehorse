import React from 'react'
import Image from 'next/image'
import exceptionSvg from './exception.svg'
import linkSvg from './link.svg'

export enum LH_EXCEPTION {
	CHILD_FAILURE = 'Child failure',
	VAR_SUB_ERROR = 'Variable sub error',
	VAR_MUTATION_ERROR = 'Variable mutation error',
	TIMEOUT = 'Timeout',
	TASK_FAILURE = 'Task failure'
}

interface FailureInformationProps {
	data: any[];
	openError: (value: any) => void;
}

export const FailureInformation = (props: FailureInformationProps) => {
	return (
		<>
			{props.data.length > 0 && (
				<>
					<div className='component-header'>
						<Image
								src={exceptionSvg}
								alt={'exception'}
								width={24}
								height={24}
						/>
						<div>
							<p>Exception log</p>
						</div>
					</div>
					<div className="drawer__task__wfrun-outputs">
						<div className='class="drawer__task__wfrun-outputs__header"'>
							<div className='drawer__task__wfrun-outputs__label'>Outputs</div>
							<div className='drawer__task__wfrun-outputs__header'>
								<p className='center'>NAME</p>
								<p className='center'>MESSAGE</p>
							</div>
						</div>
						{props.data.map((element, index: number) => {
							return (
								<>
									<div key={index}
										className='grid-2'
									>
										<p className='center'>{element.failureName}</p>
										<p className='center'>{element.message}</p>
									</div>
									<div className="drawer__task__wfrun-outputs__header grid-1 drawer__task__wfrun-outputs__header__one-column">
										<p className='center'>Output</p>
									</div>
									<div className='drawer__task__link drawer__task__link__no-border'>
										<div className='drawer__task__link__container'>
											<div
												className='drawer__task__link__container__clickable'
												onClick={() => {
													props.openError(element.log || element.message)
												}}
											>
												<Image src={linkSvg} alt={'link'} width={20} height={10} />
												<p className='drawer__task__link__container__clickable__text'>
													Exception Log
												</p>
											</div>
										</div>
									</div>
								</>
							)
						})}
					</div>

					
				</>
			)}
		</>
	)
}
