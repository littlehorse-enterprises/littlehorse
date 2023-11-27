import React from 'react'
import Image from 'next/image'
import spawnChildSvg from './spawn-child.svg'
import polylineSvg from './polyline.svg'
import type { LHException } from './FailureInformation'
import { FailureInformation } from './FailureInformation'
import type { NodeDataProps } from './NodeData'
import { NodeData } from './NodeData'
import { parseKey, getThreadName, getThreadVarName } from './drawerInternals'

interface SpawnChildInformationProps {
    linkedThread: (thread: string) => void
    nodeName: any
    errorData: {
        handlerSpecName: string
        exception: LHException | string
    }[]
    wfRunData?: {
        nodeData: NodeDataProps
    }
    setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export function SpawnChildInformation(props: SpawnChildInformationProps) {
    const onParseError = (data: any) => {
        if (typeof data === 'string') {
            props.setToggleSideBar(true, true, data, 'str')
            return
        }
        const key = parseKey(data.type.toLowerCase())
        const error = data[key]
        props.setToggleSideBar(true, true, error, key)
    }

    return (
        <>
            {
                props.nodeName ? <div className='component-header'>
                    <Image alt="spawn-child" height={24} src={spawnChildSvg} width={24}/>
                    <div>
                        <p>SpawnChild Node Information</p>
                        <p className='component-header__subheader'>{props.nodeName}</p>
                    </div>
                </div> : null
            }
            {props.wfRunData ? <NodeData {...props.wfRunData.nodeData} /> : null}
            {props.wfRunData ? <div className='drawer__startThread__wfrun__table'>
                <div className='drawer__startThread__wfrun__table__header'>
                    ExternalEvent info
                </div>
                <div className='drawer__startThread__wfrun__table__header__subheaders'>
                    <p className='drawer__nodeData__headerSimple'>NAME</p>
                    <div className='drawer__nodeData__dataSimple'>
                        <p
                            className='drawer__startThread__wfrun__table__link'
                            onClick={() => { props.linkedThread(getThreadVarName(props.nodeName)) }}
                        >
                            {getThreadName(props.nodeName)}
                            {props.nodeName}
                        </p>
                    </div>
                </div>
            </div> : null}
            {props.wfRunData === undefined && props.nodeName ? <div className='drawer__startThread__link '>
                <div className='drawer__startThread__link__title'>
                    Related threadSpec
                </div>
                <div className='drawer__startThread__link__container'>
                    <div
                        className='drawer__startThread__link__container__clickable'
                        onClick={() => { props.linkedThread(getThreadVarName(props.nodeName)) }}
                    >
                        <Image alt="polyline" src={polylineSvg} width={12}/>
                        <p className='drawer__startThread__link__container__clickable__text'>
                            {getThreadName(props.nodeName)}
                        </p>
                    </div>
                </div>
            </div> : null}
            <FailureInformation data={props.errorData} openError={onParseError}/>
        </>
    )
}
