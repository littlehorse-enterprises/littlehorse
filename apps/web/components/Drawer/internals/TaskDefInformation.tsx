import React from 'react'
import Image from 'next/image'
import linkSvg from './link.svg'
import correctArrowSvg from './correct-arrow.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'
import Link from 'next/link'

interface TaskDefInformationProps {
	linkedThread: () => void
	data:
		| { name: string; type: string; variableName: string; value?: string }[]
		| undefined
	nodeName: string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	wfRunData?: {
		nodeData: NodeDataProps
		inputs: {
			name: string
			type: string
			value: string
		}[]
		outputs: {
			type: string
			value: string
		}[]
	}
}

export const TaskDefInformation = (props: TaskDefInformationProps) => {
	return (
		<>
			<div className='component-header'>
				<Image
					src={correctArrowSvg}
					alt={'correct-arrow'}
					width={24}
					height={24}
				/>
				<div>
					<p>Task Node Information</p>
					<p className='component-header__subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunData && <NodeData {...props.wfRunData.nodeData} />}
			<div className='drawer__task__table'>
				<div className='drawer__task__table__header'>TaskDef Variables</div>
				<div
					className={`drawer__task__table__header__subheaders ${
						props.wfRunData ? 'grid-4' : 'grid-3'
					}`}
				>
					<p className='center'>
						TaskDef
						<br />
						Variable Name
					</p>
					<p className='center'>
						TaskDef
						<br />
						Variable Type
					</p>
					<p className='center'>
						Workflow
						<br />
						Variable
					</p>
					{props.wfRunData && <p className='center'>Value</p>}
				</div>
				{props.data &&
					props.data.map(
						({ name, type, variableName, value }, index: number) => {
							if (props.wfRunData)
								return (
									<div key={index} className='grid-4'>
										<p className='center'>{name}</p>
										<p className='center'>{type}</p>
										<p className='center'>{variableName}</p>
										<p className='center'>{value}</p>
									</div>
								)
							else
								return (
									<div key={index} className='grid-3'>
										<p className='center'>{name}</p>
										<p className='center'>{type}</p>
										<p className='center'>{variableName}</p>
									</div>
								)
						}
					)}
			</div>
			{props.wfRunData && (
				<>
					<div className='drawer__task__wfrun-inputs'>
						<div className='drawer__task__wfrun-inputs__label'>Inputs</div>
						<div className={'drawer__task__wfrun-inputs__header'}>
							<p className='center'>NAME</p>
							<p className='center'>TYPE</p>
							<p className='center'>VALUE</p>
						</div>
						{props.wfRunData.inputs &&
							props.wfRunData.inputs.map(
								({ name, type, value }, index: number) => {
									return (
										<div key={index} className='grid-3'>
											<p className='center'>{name}</p>
											<p className='center'>{type}</p>
											<p className='center'>{value}</p>
										</div>
									)
								}
							)}
					</div>
					<div className='drawer__task__wfrun-outputs'>
						<div className='drawer__task__wfrun-outputs__label'>Outputs</div>
						<div className='drawer__task__wfrun-outputs__header'>
							<p className='center'>TYPE</p>
							<p className='center'>VALUE</p>
						</div>
						{props.wfRunData.outputs &&
							props.wfRunData.outputs.map(({ type, value }, index: number) => {
								return (
									<div key={index} className='grid-2'>
										<p className='center'>{type}</p>
										<p className='center'>{value}</p>
									</div>
								)
							})}
					</div>
				</>
			)}
			<div className='drawer__task__link'>
				<div className='drawer__task__link__title'>TaskDef linked</div>
				<div className='drawer__task__link__container'>
					<Link href={'/taskdef/'+props?.nodeName.split('-').slice(1,-1).join('-')}
						className='drawer__task__link__container__clickable'
						style={{
							textDecoration:'none'
						}}
					>
						<Image src={linkSvg} alt={'link'} width={20} height={10} />
						<p className='drawer__task__link__container__clickable__text'>
							{props?.nodeName.split('-').slice(1,-1).join('-') || ''}
						</p>
					</Link>
					
				</div>
			</div>
			<FailureInformation data={props.errorData} />
		</>
	)
}
