'use client'
import { Calendar } from 'ui'
import { useState } from 'react'
import moment from 'moment'
import { TaskExecutionMetrics as TaskExecutionM } from '../components/metrics/TaskExecutionMetrics'

interface Props {
    id:string 
}
export function TaskExecutionMetrics({ id }:Props) {
  const [ windowLength, setWIndowLength ] = useState('HOURS_2')
  const [ windows, setWindows ] = useState(12)
  const [ lastDate, setLastDate ] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Task Execution metrics</h2> 
      <Calendar
        changeLastDate={setLastDate} changeNoWindows={setWindows}
        changeType={setWIndowLength} lastDate={lastDate}
        noWindows={windows} type={windowLength}
      />
    </div>

    <TaskExecutionM id={id} lastWindowStart={lastDate} type={windowLength} windows={windows} />

  </section>
}