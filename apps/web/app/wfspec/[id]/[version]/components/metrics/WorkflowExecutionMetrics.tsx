"use client";

import { useEffect, useState } from "react"
import moment from "moment"
import { Button, Loader } from "ui";
import { WorkflowsChart } from "../../../../../../components/Charts/WorkflowsChart";
import { LatencyChart } from "../../../../../../components/Charts/LatencyChart";

export interface taskDefMetric{
    windowStart: Date 
    type: string 
    totalStarted: string
    totalScheduled: string
    totalErrored: string
    totalCompelted: string 
    taskDefName: string
    startToCompleteMax: string 
    startToCompleteAvg: string 
    scheduleToStartMax: string 
    ScheduleToStartAvg: string
}

interface Props{
    type: string
    windows: number
    lastWindowStart: Date
    id: string 
    version?: number
}
export const WorkflowExecutionMetrics = ({id, version, windows= 16, lastWindowStart=moment().toDate(), type="HOURS_2"}:Props) => {
    const [data, setData] = useState<any[]>([])
    const [chart, setChart] = useState('workflows')
    windows = windows > 300 ? 300 : windows

    function timeoutP (_lastWindowStart:Date, data:any[]) {
        const lastWindowStart = moment(_lastWindowStart)
        let firstDate:moment.Moment
        if(type === 'HOURS_2'){
            firstDate = lastWindowStart.clone().subtract(windows*2,'hours')
        }else if(type === 'DAYS_1'){
            firstDate = lastWindowStart.clone().subtract(windows,'days')
        }else{
            const fact = (+lastWindowStart.format('mm'))%5
            console.log('FACCT',fact)
            lastWindowStart.subtract(fact,'minutes')
            firstDate = lastWindowStart.clone().subtract(windows*5,'minutes')
        }
        const out:any[] =[]
        let curr = lastWindowStart.clone()
        if(type === 'HOURS_2'){
            while (curr.format('YMMDDHH') > firstDate.format('YMMDDHH')){
                out.push({ label:curr.toString() ,data:data?.find( d => {
                    return moment.utc(d.windowStart).format('YMMDDHH') === curr.format('YMMDDHH') || moment(d.windowStart).format('YMMDDHH') === curr.clone().subtract(1,'hour').format('YMMDDHH')                    
                }) || {
                    "windowStart": curr.toString(),
                    type,
                    "taskDefName": id,
                    "scheduleToStartMax": "0",
                    "scheduleToStartAvg": "0",
                    "startToCompleteMax": "0",
                    "startToCompleteAvg": "0",
                    "totalCompleted": "0",
                    "totalErrored": "0",
                    "totalStarted": "0",
                    "totalScheduled": "0"
                }})
                curr.subtract(2,'hours')
            }
        }else if(type === 'DAYS_1'){
            while (curr.format('YMMDD') > firstDate.format('YMMDD')){
                out.push({ label:curr.toString() ,data:data?.find( d => {
                    return moment.utc(d.windowStart).format('YMMDD') === curr.format('YMMDD')
                }) || {
                    "windowStart": curr.toString(),
                    type,
                    "taskDefName": id,
                    "scheduleToStartMax": "0",
                    "scheduleToStartAvg": "0",
                    "startToCompleteMax": "0",
                    "startToCompleteAvg": "0",
                    "totalCompleted": "0",
                    "totalErrored": "0",
                    "totalStarted": "0",
                    "totalScheduled": "0"
                }})
                curr.subtract(1,'days')
            }
        }else{
            while (curr.format('YMMDDHHmm') > firstDate.format('YMMDDHHmm')){
                out.push({ label:curr.toString() ,data:data?.find( d => {
                    return moment.utc(d.windowStart).format('YMMDDHHmm') === curr.format('YMMDDHHmm')
                    
                }) || {
                    "windowStart": curr.toString(),
                    type,
                    "taskDefName": id,
                    "scheduleToStartMax": "0",
                    "scheduleToStartAvg": "0",
                    "startToCompleteMax": "0",
                    "startToCompleteAvg": "0",
                    "totalCompleted": "0",
                    "totalErrored": "0",
                    "totalStarted": "0",
                    "totalScheduled": "0"
                }})
                curr.subtract(5,'minutes')
                
                
            }
        }
        console.log('OUT',out)
        return (out.reverse())

      }

    const getData = async () => {
        const res = await fetch('/api/metrics/wfSpec',{
            method:'POST',
            body: JSON.stringify({
                lastWindowStart, 
                numWindows: windows,
                wfSpecName: id,
                wfSpecVersion: version || 0,
                windowLength: type
            }),
        })
        if(res.ok){
            const content = await res.json()
            setData( timeoutP(lastWindowStart,content.results))
        }
    }

    useEffect( () => {
        getData()
    },[windows,lastWindowStart, type])

    return (
        <article>
            <header>
                <div className=".article-title">
                </div>
                <div className="btns btns-right">
                    <Button onClick={() => setChart('workflows')} className={`btn btn-dark ${chart === 'workflows' && "active-dark"}`}>Workflows</Button>
                    <Button onClick={() => setChart('latency')} className={`btn btn-dark ${chart === 'latency' && "active-dark"}`}>Latency</Button>
                </div>
            </header>
        
            <div className={`${data.length === 0 ? 'flex items-center justify-items-center justify-center': ''}`} style={{
                height: data.length === 0 ? '400px' : 'auto'
            }}>{
                data.length > 0 ?
                <>
                    {chart === 'workflows' && <WorkflowsChart data={data} type={type}  />}
                    {chart === 'latency' && <LatencyChart data={data} type={type}  />}
                </> : <Loader />
            }</div>
        </article>
    )
}