'use client'
import { Calendar } from 'ui'
import { useState } from 'react'
import moment from 'moment'
import { TaskExecutionMetrics as TaskExecutionM } from '../components/metrics/TaskExecutionMetrics'

interface TaskExecutionMetricsProps {
    id:string 
}
export function TaskExecutionMetrics({ id }:TaskExecutionMetricsProps) {
    const [ windowLength, setWindowLength ] = useState('HOURS_2')
    const [ windows, setWindows ] = useState(12)
    const [ lastDate, setLastDate ] = useState(moment().toDate())

    return <section>
        <div className="between">
            <h2>Task Execution metrics</h2> 
            <Calendar
                changeLastDate={setLastDate} changeNoWindows={setWindows}
                changeType={setWindowLength} lastDate={lastDate}
                noWindows={windows} type={windowLength}
            />
        </div>

        <TaskExecutionM id={id} lastWindowStart={lastDate} type={windowLength} windows={windows} />

    </section>
}