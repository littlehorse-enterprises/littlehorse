"use client";

import { useState } from "react";
import { Calendar } from "ui";
import moment from "moment"

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
      </section>

    </>
  );
}
