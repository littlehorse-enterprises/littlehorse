import React, { useEffect, useState } from 'react'
import moment from 'moment'
import { DrawerHeader, DrawerSection } from 'ui'
import { parseValueByType } from '../../../helpers/parseValueByType'
import { FailureInformation } from './FailureInformation'
import type { LHException } from './FailureInformation'
import { parseKey } from './drawerInternals'

interface ExternalEventInformationProps {
    isWFRun:boolean
    run?: any
    data?: any
    wfRunId?:string
    errorData: {
        handlerSpecName: string
        exception: LHException | string
    }[]
    setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export function ExternalEventInformation({ isWFRun,run,data,wfRunId,errorData,setToggleSideBar }: ExternalEventInformationProps) {

    const onParseError = (failureData: any) => {

        if (typeof failureData  === 'string') {
            setToggleSideBar(true, true, failureData, 'str')
            return
        }
        const key = parseKey(failureData.type.toLowerCase())
        const error = failureData[key]
        setToggleSideBar(true, true, error, key)
    }

    const [ node, setNode ] = useState<any>()
    const [ externalEventRun, setExternalEventRun ] = useState<any>()
    const getNodeRun = async () => {
        const res = await fetch('/api/drawer/nodeRun', {
            method: 'POST',
            body: JSON.stringify({
                wfRunId,
                threadRunNumber: run?.number || 0,
                name: data?.positionInThreadRun
            })
        })
        if (res.ok) {
            res.json().then(result => {
                setNode(result)
            })
        }
    }
    const getExternalEventRun = async () => {
        const res = await fetch('/api/drawer/externalEvent', {
            method: 'POST',
            body: JSON.stringify({
                ...node?.externalEvent?.externalEventId
            })
        })
        if (res.ok) {
            res.json().then(result => {
                setExternalEventRun(result)
            })
        }
    }
    useEffect( () => {
        getExternalEventRun()
    },[ node ])

    useEffect( () => {
        if (isWFRun) {getNodeRun()}
    },[ isWFRun, data ])

    return (
        <>
            <DrawerHeader image="EXTERNAL_EVENT" name={data?.id} title="ExternalEvent Node Information" />

            {isWFRun ? (
                <>
                    <DrawerSection title="Node Data" >
                        <div className="grid-3">
                            {externalEventRun?.scheduledTime ? <p className="drawer__nodeData__header">SCHEDULED</p> : null}
                            {externalEventRun?.scheduledTime ? <p className="drawer__nodeData__data">{externalEventRun?.scheduledTime ? moment(externalEventRun?.scheduledTime).format('MMMM DD, HH:mm:ss') : ''}</p> : null}
                            <p className="drawer__nodeData__header">REACH TIME</p>
                            <p className="drawer__nodeData__data">{node?.arrivalTime ? moment(node.arrivalTime).format('MMMM DD, HH:mm:ss') : 'Waiting for External Event'}</p>
                            <p className="drawer__nodeData__header">COMPLETION TIME</p>
                            <p className="drawer__nodeData__data">{node?.endTime ? moment(node.endTime).format('MMMM DD, HH:mm:ss') : 'Waiting for External Event'}</p>
                            <p className="drawer__nodeData__header">STATUS</p>
                            <p className="drawer__nodeData__data">{node?.status}</p>
                        </div>
                    </DrawerSection>

                    {/* <pre>{JSON.stringify(node, null, 2)}</pre> */}

                    {/* <pre>{JSON.stringify(externalEventRun, null, 2)}</pre> */}
                    <DrawerSection title="ExternalEvent info" >
                        <div className='grid-3'>
                            <p className='drawer__nodeData__header'>GUID</p>
                            <p className='drawer__nodeData__data'>{node?.externalEvent?.externalEventId?.guid ? node?.externalEvent?.externalEventId?.guid: 'Waiting for External Event'}</p>
                            <p className='drawer__nodeData__header'>ARRIVED TIME</p>
                            <p className='drawer__nodeData__data'>
                                {node?.externalEvent?.eventTime ? moment(node.externalEvent?.eventTime).format('MMMM DD, HH:mm:ss') : 'Waiting for External Event'}
                            </p>
                            <p className='drawer__nodeData__header'>ARRIVED</p>
                            <p className='drawer__nodeData__dataSimple'>
                                {node?.externalEvent?.eventTime ? 'YES' : 'NO'}
                            </p>
                        </div>
                    </DrawerSection>

                    {externalEventRun ? <DrawerSection title="Content" >
                        <div className='grid-3'>
                            <p className='drawer__nodeData__header'>TYPE</p>
                            <p className='drawer__nodeData__data'>{externalEventRun?.content?.type ? externalEventRun?.content?.type : 'Waiting for External Event' }</p>
                            { (externalEventRun?.content?.type !== 'NULL') && <p className='drawer__nodeData__header'>VALUE</p>}
                            { (externalEventRun?.content?.type !== 'NULL') && <p className='drawer__nodeData__data'>{externalEventRun?.content ? parseValueByType(externalEventRun?.content) : 'Waiting for External Event'}</p>}
                        </div>
                    </DrawerSection> : null}
                </>
            ) : (
                <div className='drawer__externalEvent__table'>
                    <div className='drawer__externalEvent__table__header'>
                        Variables Mutations
                    </div>
                    <div className='drawer__externalEvent__table__header__subheaders'>
                        <p className='center'>MUTATED VARIABLE</p>
                        <p className='center'>MUTATION TYPE</p>
                        <p className='center'>
                            RHS
                            <br />
                            (LITERAL VALUE OR VARIABLE)
                        </p>
                    </div>
                    {data?.lhNode?.variableMutations?.map(
                        (
                            { lhsName, operation, literalValue },
                            index: number
                        ) => {
                            return (
                            // eslint-disable-next-line react/no-array-index-key -- we are using name + index
                                <div className='grid-3' key={lhsName + index}>
                                    <p className='center'>{lhsName}</p>
                                    <p className='center'>{operation}</p>
                                    <p className='center'>{literalValue ? 'Literal Value' : 'Variable' }</p>
                                    {/* <p className='center'>{literalValue ? parseliteralValue(literalValue) : parsenodeOutput(nodeOutput) }</p> */}
                                </div>
                            )
                        }
                    )}

                </div>
            )}
            <FailureInformation data={errorData} openError={onParseError} />
        </>
    )
}
