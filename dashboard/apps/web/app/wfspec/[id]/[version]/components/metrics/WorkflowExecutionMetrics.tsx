'use client'

import { useEffect, useState } from 'react'
import moment, { utc } from 'moment'
import { Button, Loader } from 'ui'
import { WorkflowsChart } from '../../../../../../components/Charts/WorkflowsChart'
import { LatencyChart } from '../../../../../../components/Charts/LatencyChart'
import { getVersionFromFormattedString } from '../common/VersionExtractor'

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

interface WorkflowExecutionMetricsProps{
    type: string
    windows: number
    lastWindowStart: Date
    id: string
    version?: number
}
export function WorkflowExecutionMetrics({ id, version, windows= 16, lastWindowStart=moment().toDate(), type='HOURS_2' }:WorkflowExecutionMetricsProps) {
    const [ data, setData ] = useState<any[]>([])
    const [ chart, setChart ] = useState('workflows')
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
        const out:any[] =[]
        const curr = lastWindowStartAsMoment.clone()
        if (type === 'HOURS_2'){
            while (curr.format('YMMDDHH') > firstDate.format('YMMDDHH')){
                out.push({ label:curr.toString() ,data:metrics?.find( d => {
                    return utc(d.windowStart).format('YMMDDHH') === curr.format('YMMDDHH') || moment(d.windowStart).format('YMMDDHH') === curr.clone().subtract(1,'hour').format('YMMDDHH')
                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': id,
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
                out.push({ label:curr.toString() ,data:metrics?.find( d => {
                    return utc(d.windowStart).format('YMMDD') === curr.format('YMMDD')
                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': id,
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
                out.push({ label:curr.toString() ,data:metrics?.find( d => {
                    return utc(d.windowStart).format('YMMDDHHmm') === curr.format('YMMDDHHmm')

                }) || {
                    'windowStart': curr.toString(),
                    type,
                    'taskDefName': id,
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
        const { majorVersion, revision } = getVersionFromFormattedString(version ? version.toString() : '0.0')
        const res = await fetch('/api/metrics/wfSpec',{
            method:'POST',
            body: JSON.stringify({
                lastWindowStart,
                numWindows: windowsNotOverpassing300,
                wfSpecName: id,
                majorVersion,
                revision,
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
    },[ windowsNotOverpassing300,lastWindowStart, type ])

    return (
        <article>
            <header>
                <div className=".article-title" />
                <div className="btns btns-right">
                    <Button className={`btn btn-dark ${chart === 'workflows' && 'active-dark'}`} onClick={() => { setChart('workflows') }}>Workflows</Button>
                    <Button className={`btn btn-dark ${chart === 'latency' && 'active-dark'}`} onClick={() => { setChart('latency') }}>Latency</Button>
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
