import moment from 'moment'
import { useEffect, useState } from 'react'
import Link from 'next/link'
import Image from 'next/image'
import { DrawerHeader, DrawerSection } from 'ui'
import { orderBy } from 'lodash'
import { parseValueByType } from '../../../helpers/parseValueByType'
import { nodename } from '../../../helpers/nodename'
import linkSvg from './link.svg'
import { FailureInformation } from './FailureInformation'

interface Props {
    isWFRun: boolean
    wfRunId?: string
    data?: any
    run?: any
    setToggleSideBar: (value: boolean) => void
    setCode: (code: any) => void
}

export function TaskInformation({ isWFRun, data, wfRunId, run, setToggleSideBar, setCode }: Props) {

  const [ attempt_no, setAttemptNo ] = useState(0)
  const [ loops, setLoops ] = useState<any[]>()
  const [ guid, setGuid ] = useState<string>()
  const [ info, setInfo ] = useState<any>()
  const [ node, setNode ] = useState<any>()
  const [ nrun, setTaskRun ] = useState<any>()
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
      res.json().then((result) => {
        setNode(result)
      })
    }
  }
  const getUserTaskRun = async () => {
    if (!node?.task?.taskRunId) return
    const res = await fetch('/api/drawer/taskRun', {
      method: 'POST',
      body: JSON.stringify(node?.task?.taskRunId)
    })
    if (res.ok) {
      res.json().then((result) => {
        setTaskRun(result)
      })
      // if(result.attempts.length)setAttempt(result.attempts[0])
    }
  }
  const getUserTaskRunGUID = async () => {
    if (!node?.task?.taskRunId) return
    const params = node?.task?.taskRunId
    params.taskGuid = guid
    const res = await fetch('/api/drawer/taskRun', {
      method: 'POST',
      body: JSON.stringify(params)
    })
    if (res.ok) {
      res.json().then(result => {
        setTaskRun(result)
      })
      // if(result.attempts.length)setAttempt(result.attempts[0])
    }
  }
  const getInfo = async () => {

    const res = await fetch('/api/information/taskDef', {
      method: 'POST',
      body: JSON.stringify({
        id: data?.lhNode?.task?.taskDefName,
      })
    })
    if (res.ok) {
      res.json().then(result => {
        setInfo(result)
      })
    }

  }
  useEffect(() => {
    if (isWFRun) getNodeRun()
  }, [ isWFRun, data ])

  useEffect(() => {
    getUserTaskRun()
  }, [ node ])

  useEffect(() => {
    getInfo()
  }, [ data ])

  const getLoops = async () => {
    const res = await fetch('/api/loops/taskRun', {
      method: 'POST',
      body: JSON.stringify({
        taskDefName: data?.lhNode?.task?.taskDefName,
        wfRunId
      }),
    })
    if (res.ok) {
      const results = await res.json()
      setLoops(results)
    }
  }

  useEffect(() => {
    if (!data) return
    getLoops()
  }, [ data ])

  useEffect(() => {
    if (!guid) return
    getUserTaskRunGUID()
  }, [ guid ])

  return (
    <>
      {loops !== undefined && Boolean(loops?.length) && loops.length > 1 && <div className="task_attempts">
        <div className="title">Node Run</div>
        <div className="selector">
          <div className="icon">
            <img src="/loop.svg"/>
          </div>
          <div className='version_select'>
            {/* <select  value={attempt_no} onChange={ e => setAttemptNo(+e.target.value)}> */}
            <select onChange={e => { setGuid(e.target.value) }}>
              {loops?.map((loop, ix: number) => <option key={ix}
                value={loop.taskGuid}> {loop.taskGuid}</option>)}
              {/* {nrun?.attempts?.map( (_, ix:number) => <option key={ix} value={ix}> {nrun?.taskDefName} [{ix}]</option>)} */}
            </select>
            <img src="/expand_more.svg" style={{ marginLeft: '30px' }}/>
          </div>
        </div>

      </div>}
      <DrawerHeader image="TASK" name={data?.id} title="Task Node Information"/>

      {isWFRun ? (
        <div className=''>
          <DrawerSection title="TaskDef Variables">
            <table>
              <thead>
                <tr>
                  <th>NAME</th>
                  <th>TYPE</th>
                  <th>VALUE</th>
                </tr>
              </thead>
              <tbody>
                {(nrun?.inputVariables !== undefined && nrun?.inputVariables.length > 0) ?
                  nrun?.inputVariables?.map((r: any, ix: number) => <tr key={ix}>
                    <td>{r.varName}</td>
                    <td>{r.value?.type}</td>
                    <td>{parseValueByType(r.value)}</td>
                  </tr>) :
                  (
                    <tr>
                      <td colSpan={3}>
                                         No Task Def Variables were found
                      </td>
                    </tr>
                  )
                }
              </tbody>
            </table>
          </DrawerSection>
          {/* <pre>{JSON.stringify(nrun?.attempts, null, 2)}</pre> */}
          <div className="task_attempts">
            <div className="title">Task Attempt</div>
            <div className="selector">
              <div className="icon">
                <img src="/replay.svg"/>
              </div>
              <div className='version_select'>
                <select onChange={e => { setAttemptNo(Number(e.target.value)) }} value={attempt_no}>
                  {nrun?.attempts?.map((_, ix: number) => <option key={ix}
                    value={ix}> {nrun?.taskDefName} [{ix}]</option>)}
                </select>
                <img src="/expand_more.svg" style={{ marginLeft: '30px' }}/>
              </div>
            </div>

          </div>

          {Boolean(nrun?.attempts.length) && <DrawerSection title="Node Data">
            <div className="grid-3">
              {nrun?.attempts?.[attempt_no || 0]?.scheduleTime ? <p className="drawer__nodeData__header">SCHEDULED</p> : null}
              {nrun?.attempts?.[attempt_no || 0]?.scheduleTime ? <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.scheduleTime ? moment(nrun?.attempts?.[attempt_no || 0]?.scheduleTime).format('MMMM DD, HH:mm:ss') : ''}</p> : null}
              <p className="drawer__nodeData__header">REACH TIME</p>
              <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.startTime ? moment(nrun?.attempts?.[attempt_no || 0]?.startTime).format('MMMM DD, HH:mm:ss') : ''}</p>
              <p className="drawer__nodeData__header">COMPLETION TIME</p>
              <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.endTime ? moment(nrun?.attempts?.[attempt_no || 0]?.endTime).format('MMMM DD, HH:mm:ss') : ''}</p>
              <p className="drawer__nodeData__header">TASK WORKER ID</p>
              <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.taskWorkerId || '-----'}</p>
              <p className="drawer__nodeData__header">STATUS</p>
              <p className="drawer__nodeData__data">{nrun?.attempts?.[attempt_no || 0]?.status}</p>
            </div>
          </DrawerSection>}
          {/* <pre>{JSON.stringify(node, null,2)}</pre> */}
          {/* <pre>{JSON.stringify(node?.task?.taskRunId, null,2)}</pre> */}
          {/* <pre>{JSON.stringify(nrun, null,2)}</pre> */}
          {/* <pre>{JSON.stringify(info, null,2)}</pre> */}
          {nrun?.attempts?.length ? <DrawerSection title="Outputs">
            <table>
              <thead>
                <tr>
                  <th>TYPE</th>
                  <th>VALUE</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  {nrun?.attempts?.[attempt_no || 0]?.output !== undefined ? (
                    <>
                      <td>{nrun?.attempts?.[attempt_no || 0]?.output?.type}</td>
                      <td>{parseValueByType(nrun?.attempts?.[attempt_no || 0]?.output)}</td>
                    </>) : <td colSpan={2}>No Output values were found</td>}
                </tr>
              </tbody>
            </table>
          </DrawerSection> : null}


        </div>
      ) : (

        <DrawerSection title="TaskDef Variables">
          <table>
            <thead>
              <tr>
                <th>NAME</th>
                <th>TYPE</th>
              </tr>
            </thead>
            <tbody>
              {(info?.inputVars !== undefined && info?.inputVars?.length > 0) ? (
                info?.inputVars?.map((f, index: number) => <tr key={index}>
                  <td>{f.name}</td>
                  <td>{f.type}</td>
                </tr>
                )) : (
                <tr>
                  <td colSpan={2}>
                                    No TaskDef Variables were found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </DrawerSection>
      )}

      <DrawerSection title="TaskDef Link">
        <Link
          className="drawer-link"
          href={
            `/taskdef/${data?.lhNode?.task?.taskDefName}`
          }
        >
          <Image alt="link" height={10} src={linkSvg} width={20}/>
          <p className="drawer__task__link__container__clickable__text">
            {data?.lhNode?.task?.taskDefName || ''}
          </p>
        </Link>
      </DrawerSection>
    </>
  )
}