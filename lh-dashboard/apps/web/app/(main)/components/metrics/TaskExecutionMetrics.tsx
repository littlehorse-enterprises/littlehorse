'use client'

import { useEffect, useState } from 'react'
import moment, { utc } from 'moment'
import { Button, Loader } from 'ui'
import { TaskChart } from '../../../../components/Charts/TaskChart'
import { LatencyTaskChart } from '../../../../components/Charts/LatencyTaskChart'

export interface TaskDefMetric {
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

interface TaskExecutionMetricsProps{
    type: string
    windows: number
    lastWindowStart: Date
}
export function TaskExecutionMetrics({ windows= 16, lastWindowStart=moment().toDate(), type='HOURS_2' }:TaskExecutionMetricsProps) {
    const [ data, setData ] = useState<any[]>([])
    const [ chart, setChart ] = useState('tasks')

    const windowsNotOverpassing300 = windows > 300 ? 300 : windows
    
    function timeoutP (_lastWindowStart:Date, metrics:any[]) {
        const lastWindowStartAsMoment = moment(_lastWindowStart)
        let firstDate:moment.Moment
        if (type === 'HOURS_2'){
            firstDate = lastWindowStartAsMoment.clone().subtract(windowsNotOverpassing300*2,'hours')
        } else if (type === 'DAYS_1'){
            firstDate = lastWindowStartAsMoment.clone().subtract(windowsNotOverpassing300,'days')
        } else {
            const fact = (Number(lastWindowStartAsMoment.format('mm')))%5
            lastWindowStartAsMoment.subtract(fact,'minutes')
            firstDate = lastWindowStartAsMoment.clone().subtract(windowsNotOverpassing300*5,'minutes')
        }
        const out:any =[]
        const curr = lastWindowStartAsMoment.clone()
        if (type === 'HOURS_2'){
            while (curr.format('YMMDDHH') > firstDate.format('YMMDDHH')){
                out.push({ label:curr.toString() ,data:metrics?.find(d => {
                    return utc(d.windowStart).format('YMMDDHH') === curr.format('YMMDDHH') || moment(d.windowStart).format('YMMDDHH') === curr.clone().subtract(1,'hour').format('YMMDDHH')
                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': 'CLUSTER_LEVEL_METRIC',
                    'scheduleToStartMax': '0',
                    'scheduleToStartAvg': '0',
                    'startToCompleteMax': '0',
                    'startToCompleteAvg': '0',
                    'totalCompleted': '0',
                    'totalErrored': '0',
                    'totalStarted': '0',
                    'totalScheduled': '0'
                } })
                curr.subtract(2,'hours')
            }
        } else if (type === 'DAYS_1'){
            while (curr.format('YMMDD') > firstDate.format('YMMDD')){
                out.push({ label:curr.toString() ,data:metrics?.find(d => {
                    return utc(d.windowStart).format('YMMDD') === curr.format('YMMDD')
                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': 'CLUSTER_LEVEL_METRIC',
                    'scheduleToStartMax': '0',
                    'scheduleToStartAvg': '0',
                    'startToCompleteMax': '0',
                    'startToCompleteAvg': '0',
                    'totalCompleted': '0',
                    'totalErrored': '0',
                    'totalStarted': '0',
                    'totalScheduled': '0'
                } })
                curr.subtract(1,'days')
            }
        } else {
            while (curr.format('YMMDDHHmm') > firstDate.format('YMMDDHHmm')){
                out.push({ label:curr.toString() ,data:metrics?.find(d => {
                    return utc(d.windowStart).format('YMMDDHHmm') === curr.format('YMMDDHHmm')
                    
                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': 'CLUSTER_LEVEL_METRIC',
                    'scheduleToStartMax': '0',
                    'scheduleToStartAvg': '0',
                    'startToCompleteMax': '0',
                    'startToCompleteAvg': '0',
                    'totalCompleted': '0',
                    'totalErrored': '0',
                    'totalStarted': '0',
                    'totalScheduled': '0'
                } })
                curr.subtract(5,'minutes')
                
                
            }
        }

        return (out.reverse())

    }

    const getData = async () => {
        const res = await fetch('/api/metrics/taskDef',{
            method:'POST',
            body: JSON.stringify({
                lastWindowStart,
                numWindows: windowsNotOverpassing300,
                taskDefName: 'CLUSTER_LEVEL_METRIC',
                windowLength: type
            }),
        })
        if (res.ok){
            const content = await res.json()
            setData( timeoutP(lastWindowStart,content.results))
        }

    }

    useEffect( () => {
        getData()
    },[ windowsNotOverpassing300, lastWindowStart, type ])

    return (
        <article>
            <header>
                <div className=".article-title">
                    <h3>Task Execution metrics</h3>
                    <h4>Cluster level</h4>
                </div>
                <div className="btns btns-right">
                    <Button className={`btn btn-dark ${chart === 'tasks' && 'active-dark'}`} onClick={() => { setChart('tasks') }}>Tasks</Button>
                    <Button className={`btn btn-dark ${chart === 'latency' && 'active-dark'}`} onClick={() => { setChart('latency') }}>Latency</Button>
                </div>
            </header>

            <div className={`${data.length === 0 ? 'flex items-center justify-items-center justify-center': ''}`} style={{
                height: data.length === 0 ? '400px' : 'auto'
            }}>{
                    data.length > 0 ? <>
                        {chart === 'tasks' && <TaskChart data={data} type={type} />}
                        {chart === 'latency' && <LatencyTaskChart data={data} type={type}  />}
                    </> : <Loader />
                }</div>
        </article>
    )
}