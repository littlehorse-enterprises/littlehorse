import React from 'react'
import Image from 'next/image'
import spawnChildSvg from './spawn-child.svg'
import polylineSvg from './polyline.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'

interface WaitChildInformationProps {
	linkedThread: () => void
	nodeName: string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	wfRunDrawer: boolean
}

export const WaitChildInformation = (props: WaitChildInformationProps) => {
	return (
		<>
			<div className='flex justify-center gap-3 m-3'>
				<Image src={spawnChildSvg} alt={'spawn-child'} width={24} height={24} />
				<div>
					<p>WaitForChild Node Information</p>
					<p className='lh-subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunDrawer ? (
				<div className='lh-border lh-round flex flex-col overflow-y-auto'>
					<div className='p-3 text-center lh-drawer-title'>
						ExternalEvent info
					</div>
					<div className='grid grid-cols-3'>
						<p className='text-center p-3 lh-node-data-header-single'>NAME</p>
						<div className='text-center p-3 col-span-2'>
							<p className='lh-link' onClick={() => props.linkedThread()}>
								spawned-thread
							</p>
						</div>
					</div>
				</div>
			) : (
				<div className='flex flex-col overflow-y-auto lh-border lh-round'>
					<div className='p-3 text-center lh-drawer-title'>
						Related threadSpec
					</div>
					<div className='flex justify-center m-3'>
						<div
							className='cursor-pointer flex gap-3'
							onClick={() => props.linkedThread()}
						>
							<Image src={polylineSvg} alt={'polyline'} width={12} />
							<p className='lh-link'>spawned-thread</p>
						</div>
					</div>
				</div>
			)}
			{<FailureInformation data={props.errorData} />}
		</>
	)
}
