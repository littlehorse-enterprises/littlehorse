import React from 'react'
import Image from 'next/image'
import spawnChildSvg from './spawn-child.svg'
import polylineSvg from './polyline.svg'
import { FailureInformation, LH_EXCEPTION } from './FailureInformation'
import { parseKey, getThreadName } from './drawerInternals'

interface WaitChildInformationProps {
	linkedThread: () => void
	nodeName: string
	errorData: {
		handlerSpecName: string
		exception: LH_EXCEPTION | string
	}[]
	wfRunDrawer?: boolean
	setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export const WaitChildInformation = (props: WaitChildInformationProps) => {
	const onParseError = (data: any) => {
		if (typeof data  == 'string') {
			props.setToggleSideBar(true, true, data, 'str')
			return;
		}
		const key = parseKey(data.type.toLowerCase());
		const error = data[key];
		props.setToggleSideBar(true, true, error, key)
	}
	return (
		<>
			<div className='component-header'>
				<Image src={spawnChildSvg} alt={'spawn-child'} width={24} height={24} />
				<div>
					<p>WaitForChild Node Information</p>
					<p className='component-header__subheader'>{props.nodeName}</p>
				</div>
			</div>
			{props.wfRunDrawer ? (
				<div className='drawer__waitChild__wfrun__link'>
					<div className='drawer__waitChild__wfrun__link__title'>
						ExternalEvent info
					</div>
					<div className='grid-3'>
						<p className='drawer__nodeData__headerSimple'>NAME</p>
						<div className='drawer__waitChild__wfrun__link__container'>
							<p
								className='drawer__waitChild__wfrun__link__container__text'
								onClick={() => props.linkedThread()}
							>
								{getThreadName(props.nodeName)}
							</p>
						</div>
					</div>
				</div>
			) : (
				<div className='drawer__waitChild__link '>
					<div className='drawer__waitChild__link__title'>
						Related threadSpec
					</div>
					<div className='drawer__waitChild__link__container'>
						<div
							className='drawer__waitChild__link__container__clickable'
							onClick={() => props.linkedThread()}
						>
							<Image src={polylineSvg} alt={'polyline'} width={12} />
							<p className='drawer__waitChild__link__container__clickable__text'>
								{getThreadName(props.nodeName)}
							</p>
						</div>
					</div>
				</div>
			)}
			{<FailureInformation data={props.errorData} openError={onParseError} />}
		</>
	)
}
