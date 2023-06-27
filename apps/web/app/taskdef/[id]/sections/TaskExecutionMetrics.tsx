"use client";
import { Calendar } from "ui"
import { useState } from "react";
import moment from "moment"
import { TaskExecutionMetrics as TaskExecutionM } from "../components/metrics/TaskExecutionMetrics";

interface Props {
    id:string 
}
export const TaskExecutionMetrics = ({id}:Props) => {
  const [windowLength, setWIndowLength] = useState('HOURS_2');
  const [windows, setWindows] = useState(12)
  const [lastDate, setLastDate] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Task Execution metrics</h2> 
      <Calendar
        changeType={setWIndowLength} type={windowLength}
        changeLastDate={setLastDate} lastDate={lastDate}
        changeNoWindows={setWindows} noWindows={windows}
        />
    </div>

    <TaskExecutionM type={windowLength} windows={windows} lastWindowStart={lastDate} id={id} />

  </section>
}