import moment from 'moment'
import { useEffect, useState } from 'react'
import SleepLabelExtractor from './extractors/SleepLabelExtractor'

interface SleepNodeInformationProps {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
}
export function SleepNodeInformation({ isWFRun, data, wfRunId, run }:SleepNodeInformationProps) {

    const [ nodeRun, setNodeRun ] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
            method: 'POST',
            body: JSON.stringify({
                wfRunId,
                threadRunNumber: run?.number || 0,
                name:data?.positionInThreadRun
            })
        })
        if (res.ok) {
            res.json().then(result => {
                setNodeRun(result)
            })
        }
       
    }
    useEffect( () => {
        if (isWFRun) {getNodeRun()}
    },[ isWFRun ])
    return (
        <>
            <div className='component-header'>
                <img alt="sleep" src="/SLEEP.svg" />
                <div>
                    <p>Sleep Node Information</p>
                    <p className='component-header__subheader'>{data?.id?.split('-').slice(0,-1).join('-')}</p>
                </div>
            </div>
            {isWFRun ? (
                <div className='drawer__waitChild__wfrun__link'>
                    <div className='drawer__waitChild__wfrun__link__title'>
                        Maturation Time
                    </div>
                    <div className='drawer__waitChild__link__container'>
                        <div className='simpleValue__container' >
                            <p className='simpleValue'>
                                {nodeRun?.sleep?.maturationTime ? moment(nodeRun?.sleep?.maturationTime).format('MMMM D, Y. h:mm A') : ''}
                            </p>
                        </div>
                    </div>
                </div>
            ) : (
                <div className='drawer__waitChild__link '>
                    <div className='drawer__waitChild__link__title'>
                        Sleep Until
                    </div>
                    <div className='drawer__waitChild__link__container'>
                        <div className='simpleValue__container' >
                            <p className='simpleValue'>
                                {SleepLabelExtractor.extract(data?.lhNode?.sleep)}
                            </p>
                        </div>
       
                    </div>
                </div>
            )}
            {/* <FailureInformation data={errorData} openError={onParseError} /> */}
            {/* data.nodeRun.failureHandlers */}
        </>
    )
}
