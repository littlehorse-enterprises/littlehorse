"use client";
import { Calendar } from "ui"
import { WorkflowExecutionMetrics } from "../components/metrics/WorkflowExecutionMetrics"
import { TaskExecutionMetrics } from "../components/metrics/TaskExecutionMetrics"
import { useState } from "react";
import moment from "moment"

export const Metrics = () => {
  const [windowLength, setWIndowLength] = useState('HOURS_2');
  const [windows, setWindows] = useState(12)
  const [lastDate, setLastDate] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Metrics</h2> 
      <Calendar
        changeType={setWIndowLength} type={windowLength}
        changeLastDate={setLastDate} lastDate={lastDate}
        changeNoWindows={setWindows} noWindows={windows}
        />
    </div>

    <WorkflowExecutionMetrics type={windowLength} windows={windows} lastWindowStart={lastDate} />
    <TaskExecutionMetrics type={windowLength} windows={windows} lastWindowStart={lastDate} />

  </section>
}