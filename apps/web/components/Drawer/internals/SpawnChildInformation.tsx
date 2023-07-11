import React from 'react'
import Image from 'next/image'
import spawnChildSvg from './spawn-child.svg'
import polylineSvg from './polyline.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { NodeData, NodeDataProps } from './NodeData'

interface SpawnChildInformationProps {
	linkedThread: () => void
	nodeName: any
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	wfRunData?: {
		nodeData: NodeDataProps
	}
}

export const SpawnChildInformation = (props: SpawnChildInformationProps) => {
	return (
		<>
			<div className='component-header'>
				<Image src={spawnChildSvg} alt={'spawn-child'} width={24} height={24} />
				<div>
					<p>SpawnChild Node Information</p>
					<p className='component-header__subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunData && <NodeData {...props.wfRunData.nodeData} />}
			{props.wfRunData && (
				<>
					<div className='drawer__startThread__wfrun__table'>
						<div className='drawer__startThread__wfrun__table__header'>
							ExternalEvent info
						</div>
						<div className='drawer__startThread__wfrun__table__header__subheaders'>
							<p className='drawer__nodeData__headerSimple'>NAME</p>
							<div className='drawer__nodeData__dataSimple'>
								<p
									className='drawer__startThread__wfrun__table__link'
									onClick={() => props.linkedThread()}
								>
									spawned-thread
								</p>
							</div>
						</div>
					</div>
				</>
			)}
			{props.wfRunData == undefined && (
				<div className='drawer__startThread__link '>
					<div className='drawer__startThread__link__title'>
						Related threadSpec
					</div>
					<div className='drawer__startThread__link__container'>
						<div
							className='drawer__startThread__link__container__clickable'
							onClick={() => props.linkedThread()}
						>
							<Image src={polylineSvg} alt={'polyline'} width={12} />
							<p className='drawer__startThread__link__container__clickable__text'>
								spawned-thread
							</p>
						</div>
					</div>
				</div>
			)}
			<FailureInformation data={props.errorData} />
		</>
	)
}
