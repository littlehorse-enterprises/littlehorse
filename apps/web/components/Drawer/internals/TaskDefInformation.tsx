import React from 'react'
import Image from 'next/image'
import linkSvg from './link.svg'
import correctArrowSvg from './correct-arrow.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'
import { parseKey } from './drawerInternals'

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
	},
	setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export const TaskDefInformation = (props: TaskDefInformationProps) => {

	const onParseError = (data: any) => {
		if (typeof data  == 'string') {
			props.setToggleSideBar(true, true, data, 'str')
			return;
		}
		const key = parseKey(data.type.toLowerCase());
		const error = data[key];
		props.setToggleSideBar(true, true, error, key)
	}

	const LinkToSnipper = (value: any) => <button className='btn btn-wfrun-link' onClick={(e) => {
		props.setToggleSideBar(true, false, value.data)
	}}>See More</button>

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
							let link;
							if (type === 'JSON_OBJ' || type === 'JSON_ARR') {
								link = <LinkToSnipper data={value} />
							}
							if (props.wfRunData)
								return (
									<div key={index} className='grid-4'>
										<p className='center'>{name}</p>
										<p className='center'>{type}</p>
										<p className='center'>{variableName}</p>
										<p className='center'>{link ? link : value}</p>
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
									let link;
									if (type === 'JSON_OBJ' || type === 'JSON_ARR') {
										link = <LinkToSnipper data={value} />
									}
									return (
										<div key={index} className='grid-3'>
											<p className='center'>{name}</p>
											<p className='center'>{type}</p>
											<p className='center'>{link ? link : value}</p>
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
								let link;
								if (type === 'JSON_OBJ' || type === 'JSON_ARR') {
									link = <LinkToSnipper data={value} />
								}
								return (
									<div key={index} className='grid-2'>
										<p className='center'>{type}</p>
										<p className='center'>{link ? link : value}</p>
									</div>
								)
							})}
					</div>
				</>
			)}
			<div className='drawer__task__link'>
				<div className='drawer__task__link__title'>TaskDef linked</div>
				<div className='drawer__task__link__container'>
					<div
						className='drawer__task__link__container__clickable'
						onClick={() => props.linkedThread()}
					>
						<Image src={linkSvg} alt={'link'} width={20} height={10} />
						<p className='drawer__task__link__container__clickable__text'>
							request-confirmation
						</p>
					</div>
				</div>
			</div>
			<FailureInformation data={props.errorData} openError={onParseError} />
		</>
	)
}
