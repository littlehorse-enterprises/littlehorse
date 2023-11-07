import React, { useEffect, useState } from 'react'
import moment from 'moment'
import { DrawerHeader, DrawerSection, DrawerThreadSpecLink } from 'ui'
import { nodename, nodeposition } from '../../../helpers/nodename'
import { FailureInformation } from './FailureInformation'
import type { LH_EXCEPTION } from './FailureInformation'
import { parseKey } from './drawerInternals'
import ThreadRunsHandler from './ThreadRunsHandler'

interface Props {
    linkedThread: (thread: string) => void
    isWFRun: boolean
    run?: any
    threadRuns?: any
    data?: any
    wfRunId?: string,
    setThreadRunInfoValue?: (threadRunInfoValue: string) => void,
    errorData: {
        handlerSpecName: string
        exception: LH_EXCEPTION | string
    }[]
    setToggleSideBar: (value: boolean, isError: boolean, code: string, language?: string) => void;
}

export function WaitForThreadsInformation({
  isWFRun,
  setThreadRunInfoValue,
  run,
  threadRuns,
  data,
  wfRunId,
  errorData,
  setToggleSideBar,
  linkedThread
}: Props) {

  const onParseError = (data: any) => {

    if (typeof data === 'string') {
      setToggleSideBar(true, true, data, 'str')
      return
    }
    const key = parseKey(data.type.toLowerCase())
    const error = data[key]
    setToggleSideBar(true, true, error, key)
  }

  const [ nodeRun, setNodeRun ] = useState<any>()
  const getNodeRun = async () => {
    const res = await fetch('/api/drawer/nodeRun', {
      method: 'POST',
      body: JSON.stringify({
        wfRunId,
        threadRunNumber: run?.number || 0,
        name: data.positionInThreadRun
      })
    })
    if (res.ok) {
      res.json().then(result => {
        setNodeRun(result)
      })
    }
  }

  useEffect(() => {
    if (isWFRun) {
      getNodeRun()
    }
  }, [ isWFRun, data ])

  return (
    <>
      <DrawerHeader image="WAIT_FOR_THREADS" name={data?.id} title="WaitForThreads Node Information"/>
      {isWFRun ? (
        <>
          <DrawerSection title="Node Data">
            <div className="grid-3">
              {nodeRun?.scheduledTime ? <p className="drawer__nodeData__header">SCHEDULED</p> : null}
              {nodeRun?.scheduledTime ?
                <p className="drawer__nodeData__data">{nodeRun?.scheduledTime ? moment(nodeRun?.scheduledTime).format('MMMM DD, HH:mm:ss') : ''}</p> : null}
              <p className="drawer__nodeData__header">REACH TIME</p>
              <p className="drawer__nodeData__data">{nodeRun?.arrivalTime ? moment(nodeRun.arrivalTime).format('MMMM DD, HH:mm:ss') : ''}</p>
              <p className="drawer__nodeData__header">COMPLETION TIME</p>
              <p className="drawer__nodeData__data">{nodeRun?.endTime ? moment(nodeRun.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
              <p className="drawer__nodeData__header">STATUS</p>
              <p className="drawer__nodeData__data">{nodeRun?.status}</p>
            </div>
          </DrawerSection>
          {nodeRun !== undefined &&
                      <DrawerSection title="Related ThreadRun">
                        {
                          ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(nodeRun, threadRuns).length === 0 ?
                            <div className="grid-3 center">
                                      No related thread specs were found
                            </div> :
                            ThreadRunsHandler.getThreadRunsAssociatedWithNodeRun(nodeRun, threadRuns).map(relatedThreadRun => {
                              return <DrawerThreadSpecLink key={relatedThreadRun.number}
                                label={`${relatedThreadRun.number}-${relatedThreadRun.threadSpecName}`}
                                name={ThreadRunsHandler.buildThreadRunInfo(relatedThreadRun.number, relatedThreadRun.threadSpecName)}
                                onClick={setThreadRunInfoValue!}/>
                            })
                        }
                      </DrawerSection>
          }
        </>
      ) : (
        <DrawerSection title="Related ThreadSpec">
          {data?.lhNode?.waitForThreads?.threads && data?.lhNode?.waitForThreads?.threads.length > 0 ?
            data?.lhNode?.waitForThreads?.threads?.map((t: any, ix: number) =>
              <DrawerThreadSpecLink key={ix}
                label={nodename(t.threadRunNumber?.variableName)}
                name={nodename(t.threadRunNumber?.variableName)}
                onClick={linkedThread}/>
            ) :
            (
              <div className="grid-3 center">
                                No related thread specs were found
              </div>
            )
          }
        </DrawerSection>
      )}

      <FailureInformation data={errorData} openError={onParseError}/>
    </>
  )
}
