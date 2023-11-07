'use client'
import { Calendar } from 'ui'
import { useState } from 'react'
import moment from 'moment'
import { WorkflowExecutionMetrics as WorkflowExecutionM } from '../components/metrics/WorkflowExecutionMetrics'

interface Props {
    id:string 
    version?: number
}
export function WorkflowExecutionMetrics({ id,version }:Props) {
  const [ windowLength, setWIndowLength ] = useState('HOURS_2')
  const [ windows, setWindows ] = useState(12)
  const [ lastDate, setLastDate ] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Workflow Execution metrics</h2> 
      <Calendar
        changeLastDate={setLastDate} changeNoWindows={setWindows}
        changeType={setWIndowLength} lastDate={lastDate}
        noWindows={windows} type={windowLength}
      />
    </div>

    <WorkflowExecutionM id={id} lastWindowStart={lastDate} type={windowLength} version={version} windows={windows}/>

  </section>
}