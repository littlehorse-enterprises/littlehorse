import React from 'react'
import Image from 'next/image'
import linkSvg from './link.svg'
import correctArrowSvg from './correct-arrow.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'

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
	wfRunDrawer: boolean
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
			<div className='flex justify-center gap-3 m-3'>
				<Image
					src={correctArrowSvg}
					alt={'correct-arrow'}
					width={24}
					height={24}
				/>
				<div>
					<p>Task Node Information</p>
					<p className='lh-subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunDrawer && <NodeData {...props.wfRunData!.nodeData} />}
			<div className='flex flex-col overflow-y-auto lh-border lh-round'>
				<div className='p-3 text-center lh-drawer-title'>TaskDef Variables</div>
				<div
					className={`lh-drawer-headers items-center grid ${
						props.wfRunDrawer ? 'grid-cols-4' : 'grid-cols-3'
					}`}
				>
					<p className='p-3 text-center'>
						TaskDef
						<br />
						Variable Name
					</p>
					<p className='p-3 text-center'>
						TaskDef
						<br />
						Variable Type
					</p>
					<p className='p-3 text-center'>
						Workflow
						<br />
						Variable
					</p>
					{props.wfRunDrawer && <p className='p-3 text-center'>Value</p>}
				</div>
				{props.data &&
					props.data.map(
						({ name, type, variableName, value }, index: number) => {
							if (props.wfRunDrawer)
								return (
									<div key={index} className='grid grid-cols-4 items-center'>
										<p className='text-center p-3'>{name}</p>
										<p className='text-center p-3'>{type}</p>
										<p className='text-center p-3'>{variableName}</p>
										<p className='text-center p-3'>{value}</p>
									</div>
								)
							else
								return (
									<div key={index} className='grid grid-cols-3 items-center'>
										<p className='text-center p-3'>{name}</p>
										<p className='text-center p-3'>{type}</p>
										<p className='text-center p-3'>{variableName}</p>
									</div>
								)
						}
					)}
			</div>

			{props.wfRunDrawer && (
				<>
					<div className='flex flex-col overflow-y-auto lh-border lh-round'>
						<div className='p-3 text-center lh-drawer-title'>Inputs</div>
						<div className={'lh-drawer-headers items-center grid grid-cols-3'}>
							<p className='p-3 text-center'>NAME</p>
							<p className='p-3 text-center'>TYPE</p>
							<p className='p-3 text-center'>VALUE</p>
						</div>
						{props.wfRunData &&
							props.wfRunData.inputs.map(
								({ name, type, value }, index: number) => {
									return (
										<div key={index} className='grid grid-cols-3 items-center'>
											<p className='text-center p-3'>{name}</p>
											<p className='text-center p-3'>{type}</p>
											<p className='text-center p-3'>{value}</p>
										</div>
									)
								}
							)}
					</div>
					<div className='flex flex-col overflow-y-auto lh-border lh-round'>
						<div className='p-3 text-center lh-drawer-title'>Outputs</div>
						<div className={'lh-drawer-headers items-center grid grid-cols-2'}>
							<p className='p-3 text-center'>TYPE</p>
							<p className='p-3 text-center'>VALUE</p>
						</div>
						{props.wfRunData &&
							props.wfRunData.outputs.map(({ type, value }, index: number) => {
								return (
									<div key={index} className='grid grid-cols-2 items-center'>
										<p className='text-center p-3'>{type}</p>
										<p className='text-center p-3'>{value}</p>
									</div>
								)
							})}
					</div>
				</>
			)}
			<div className='lh-border lh-round flex flex-col overflow-y-auto'>
				<div className='lh-drawer-title text-center p-3'>TaskDef linked</div>
				<div className='flex justify-center m-3'>
					<div
						className='cursor-pointer flex gap-3'
						onClick={() => props.linkedThread()}
					>
						<Image src={linkSvg} alt={'link'} width={20} height={10} />
						<p className='lh-link'>request-confirmation</p>
					</div>
				</div>
			</div>
			<FailureInformation data={props.errorData} />
		</>
	)
}
