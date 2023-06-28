import React from 'react'
import Image from 'next/image'
import arrowRightSvg from './arrow-right.svg'

interface DrawerProps {
	title: string
	children?: React.ReactNode
}

export const Drawer = (props: DrawerProps) => {
	return (
		<div className='w-full h-full'>
			<div className='lh-border flex flex-col p-4 gap-3 m-2.5 overflow-y-auto'>
				<div className='flex p-2.5 gap-2.5'>
					<button onClick={() => console.warn('Missing clickHandler')}>
						<Image
							src={arrowRightSvg}
							alt={'arrow-right'}
							width={24}
							height={24}
						/>
					</button>
					<p className='ml-2'>{props.title}</p>
				</div>
				{props.children}
			</div>
		</div>
	)
}
