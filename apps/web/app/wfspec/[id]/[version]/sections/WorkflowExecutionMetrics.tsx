"use client";
import { Calendar } from "ui"
import { useState } from "react";
import moment from "moment"
import { WorkflowExecutionMetrics as WorkflowExecutionM } from "../components/metrics/WorkflowExecutionMetrics";

interface Props {
    id:string 
    version?: number
}
export const WorkflowExecutionMetrics = ({id,version}:Props) => {
  const [windowLength, setWIndowLength] = useState('HOURS_2');
  const [windows, setWindows] = useState(12)
  const [lastDate, setLastDate] = useState(moment().toDate())

  return <section>
    <div className="between">
      <h2>Workflow Execution metrics</h2> 
      <Calendar
        changeType={setWIndowLength} type={windowLength}
        changeLastDate={setLastDate} lastDate={lastDate}
        changeNoWindows={setWindows} noWindows={windows}
        />
    </div>

    <WorkflowExecutionM type={windowLength} windows={windows} lastWindowStart={lastDate} id={id} version={version}/>

  </section>
}