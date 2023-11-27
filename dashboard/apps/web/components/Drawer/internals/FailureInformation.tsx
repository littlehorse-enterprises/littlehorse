import React from 'react'
import Image from 'next/image'
import exceptionSvg from './exception.svg'
import linkSvg from './link.svg'

export enum LHException {
    ChildFailure = 'Child failure',
    VarSubError = 'Variable sub error',
    VarMutationError = 'Variable mutation error',
    Timeout = 'Timeout',
    TaskFailure = 'Task failure'
}

interface FailureInformationProps {
    data: any[];
    openError: (value: any) => void;
}

export function FailureInformation(props: FailureInformationProps) {
    return (
        <>
            {props.data.length > 0 && (
                <>
                    <div className='component-header'>
                        <Image
                            alt="exception"
                            height={24}
                            src={exceptionSvg}
                            width={24}
                        />
                        <div>
                            <p>Exception log</p>
                        </div>
                    </div>
                    <div className="drawer__task__wfrun-outputs">
                        <div className='class="drawer__task__wfrun-outputs__header"'>
                            <div className='drawer__task__wfrun-outputs__label'>Outputs</div>
                            <div className='drawer__task__wfrun-outputs__header'>
                                <p className='center'>NAME</p>
                                <p className='center'>MESSAGE</p>
                            </div>
                        </div>
                        {props.data.map((element, index: number) => {
                            return (
                                <>
                                    {/* eslint-disable-next-line react/no-array-index-key -- using name + index */}
                                    <div className='grid-2' key={element.failureName + index}>
                                        <p className='center'>{element.failureName}</p>
                                        <p className='center drawer__task__wfrun-outputs__log-message'>{element.message !== undefined ? `${element.message.substring(0, 200)}...` : ''}</p>
                                    </div>
                                    <div className="drawer__task__wfrun-outputs__header grid-1 drawer__task__wfrun-outputs__header__one-column">
                                        <p className='center'>Output</p>
                                    </div>
                                    <div className='drawer__task__link drawer__task__link__no-border'>
                                        <div className='drawer__task__link__container'>
                                            <div
                                                className='drawer__task__link__container__clickable'
                                                onClick={() => {
                                                    props.openError(element.log || element.message)
                                                }}
                                            >
                                                <Image alt="link" height={10} src={linkSvg} width={20} />
                                                <p className='drawer__task__link__container__clickable__text'>
                                                    Exception Log
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                </>
                            )
                        })}
                    </div>


                </>
            )}
        </>
    )
}
