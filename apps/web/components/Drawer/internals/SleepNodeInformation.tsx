import moment from "moment"
import { useEffect, useState } from "react"
import { FailureInformation } from "./FailureInformation"

interface Props {
    isWFRun:boolean
    wfRunId?:string
    data?: any
    run?: any
}
export const SleepNodeInformation = ({isWFRun, data, wfRunId, run}:Props) => {

    const [node, setNode] = useState<any>()
    const getNodeRun = async () => {

        const res = await fetch('/api/drawer/nodeRun', {
			method: 'POST',
			body: JSON.stringify({
				wfRunId,
				threadRunNumber: run?.number || 0,
                name:data?.name?.split('-')[0] || 0
			})
		})
        if (res.ok) {
			const {result} = await res.json()
            setNode(result)
		}
       
    }
    useEffect( () => {
        if(isWFRun) getNodeRun()
    },[isWFRun])
    return (
        <>
        <div className='component-header'>
            <img src={`/SLEEP.svg`} alt="sleep" />
            <div>
                <p>Sleep Node Information</p>
                <p className='component-header__subheader'>{data?.name && data.name.split('-').slice(0,-1).join('-')}</p>
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
                            {node?.sleep?.maturationTime ? moment(node?.sleep?.maturationTime).format('MMMM D, Y. h:mm A') : ''}
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

                            {data?.node?.sleep?.rawSeconds?.variableName || ''}
                            {data?.node?.sleep?.timestamp?.variableName || ''}
                            {data?.node?.sleep?.isoDate?.variableName || ''}

                        </p>
                    </div>
       
                </div>
            </div>
        )}
        {/* <FailureInformation data={errorData} openError={onParseError} /> */}
        {/* data.node.failureHandlers */}
        </>
    )
}