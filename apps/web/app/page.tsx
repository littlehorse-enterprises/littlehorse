"use client";

import { useState } from "react";
import { Button, Calendar } from "ui";
import moment from "moment"
import { WorkflowExecutionMetrics } from "./components/metrics/WorkflowExecutionMetrics";
import { TaskExecutionMetrics } from "./components/metrics/TaskExecutionMetrics";
import { MetadataSearch } from "./components/tables/MetadataSearch";

export default function Page() {
  const [windowLength, setWIndowLength] = useState('HOURS_2');
  const [windows, setWindows] = useState(12)
  const [lastDate, setLastDate] = useState(moment().toDate())

  return (
    <>
      <h1>Cluster Overview </h1>
      <section>
        <div className="between">
        <h2>Metrics</h2> 
          <Calendar
            changeType={setWIndowLength} type={windowLength}
            changeLastDate={setLastDate} lastDate={lastDate}
            changeNoWindows={setWindows} noWindows={windows}
            />
        </div>

        <div className="grid gap-4 md:grid-cols-1">
                        <div className="grid xs:grid-cols-1 md:grid-cols-1 workflow-dashboard"
                        style={{
                            background: "linear-gradient(180deg, #282B30 0%, #24272B 53.65%, #232529 100%)",
                            boxShadow: "0px 2px 2px 0px rgba(0, 0, 0, 0.10), 0px 6px 6px 0px rgba(0, 0, 0, 0.06), 0px 12px 18px 0px rgba(0, 0, 0, 0.05)",
                            minWidth:"1470px"
                        }}
                        >
                            <WorkflowExecutionMetrics type={windowLength} windows={windows} lastWindowStart={lastDate} />
                        </div>
                        <div className="grid xs:grid-cols-2 md:grid-cols-1 task-dashboard"
                            style={{
                                background: "linear-gradient(180deg, #282B30 0%, #24272B 53.65%, #232529 100%)",
                                boxShadow: "0px 2px 2px 0px rgba(0, 0, 0, 0.10), 0px 6px 6px 0px rgba(0, 0, 0, 0.06), 0px 12px 18px 0px rgba(0, 0, 0, 0.05)",
                                minWidth:"1470px"
                            }}
                            >
                            <TaskExecutionMetrics type={windowLength} windows={windows} lastWindowStart={lastDate} />
                        </div>
                    </div>

      </section>
      
      <section>
        <div className="between">
        <h2>Metadata search</h2> 
          <Button>-test-</Button>
        </div>
        <MetadataSearch />
      </section>
    </>
  );
}
