import React from 'react'
import Image from 'next/image'
import arrowRightSvg from './arrow-right.svg'

interface DrawerProps {
	title: string
	children?: React.ReactNode
}

export const Drawer = (props: DrawerProps) => {
	return (
		<div className='drawer scrollbar'>
			<header className='drawer__header'>
				<button
					className='drawer__header__button'
					onClick={() => console.warn('Missing clickHandler')}
				>
					<Image
						src={arrowRightSvg}
						alt={'arrow-right'}
						width={24}
						height={24}
					/>
				</button>
				<p>{props.title}</p>
			</header>
			{props.children}
		</div>
	)
}
