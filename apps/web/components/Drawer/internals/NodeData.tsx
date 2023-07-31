import React from 'react'

export interface NodeDataProps {
	reachTime: string
	completionTime: string
	status: string
}

export const NodeData = (props: NodeDataProps) => {
	return (
		<div className='drawer__nodeData'>
			<div className='drawer__nodeData__label'>Node data</div>
			<div className='grid-3'>
				<p className='drawer__nodeData__header'>REACH TIME</p>
				<p className='drawer__nodeData__data'>{props.reachTime}</p>
				<p className='drawer__nodeData__header'>COMPLETION TIME</p>
				<p className='drawer__nodeData__data'>{props.completionTime}</p>
				<p className='drawer__nodeData__headerSimple'>STATUS</p>
				<p className='drawer__nodeData__dataSimple'>{props.status}</p>
			</div>
		</div>
	)
}
