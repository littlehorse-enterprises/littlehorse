'use client'
import { Calendar } from 'ui'
import { useState } from 'react'
import moment from 'moment'
import { WorkflowExecutionMetrics } from '../components/metrics/WorkflowExecutionMetrics'
import { TaskExecutionMetrics } from '../components/metrics/TaskExecutionMetrics'

export function Metrics() {
  const [ windowLength, setWIndowLength ] = useState('HOURS_2')
  const [ windows, setWindows ] = useState(24)
  const [ lastDate, setLastDate ] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Metrics</h2> 
      <Calendar
        changeLastDate={setLastDate} changeNoWindows={setWindows}
        changeType={setWIndowLength} lastDate={lastDate}
        noWindows={windows} type={windowLength}
      />
    </div>

    <WorkflowExecutionMetrics lastWindowStart={lastDate} type={windowLength} windows={windows} />
    <TaskExecutionMetrics lastWindowStart={lastDate} type={windowLength} windows={windows} />

  </section>
}