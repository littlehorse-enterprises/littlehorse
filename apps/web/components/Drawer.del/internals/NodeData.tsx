import React from 'react'

export interface NodeDataProps {
	scheduled: string
	reachTime: string
	completionTime: string
	status: string
}

export const NodeData = (props: NodeDataProps) => {
	return (
		<div className='lh-border lh-round flex flex-col overflow-y-auto'>
			<div className='p-3 text-center lh-drawer-title'>Node data</div>
			<div className='grid grid-cols-3'>
				<p className='text-center p-3 lh-node-data-header'>SCHEDULED</p>
				<p className='text-center p-3 col-span-2 lh-node-data'>
					{props.scheduled}
				</p>
				<p className='text-center p-3 lh-node-data-header'>REACH TIME</p>
				<p className='text-center p-3 col-span-2 lh-node-data'>
					{props.reachTime}
				</p>
				<p className='text-center p-3 lh-node-data-header'>COMPLETION TIME</p>
				<p className='text-center p-3 col-span-2 lh-node-data'>
					{props.completionTime}
				</p>
				<p className='text-center p-3 lh-node-data-header-single'>STATUS</p>
				<p className='text-center p-3 col-span-2'>{props.status}</p>
			</div>
		</div>
	)
}
