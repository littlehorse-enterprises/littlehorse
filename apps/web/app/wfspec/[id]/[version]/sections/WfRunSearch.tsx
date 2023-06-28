"use client";
import { Button, Calendar, CalendarB, Label, LoadMoreButton, PerPage } from "ui"
import { useEffect, useState } from "react"
import moment from "moment";
import { WfRunSearchTable } from "../components/search/WfRunSearchTable";

export interface Result{
    id:string 
    status?:string 
}

const allLimit = 5
const defaultLimit = 15
const keyDownDelay = 1000 // miliseconds

let myTimeout:NodeJS.Timeout 

export const WfRunSearch = ({id, version}:any) => {
    let first = true

    const [loading, setLoading] = useState(false)
    const [firstLoad, setFirstLoad] = useState(false)
    const [limit, setLimit] = useState(defaultLimit)

    const [startDt, setStartDT] = useState<Date>(moment().startOf('day').toDate())
    const [endDt, setEndDT] = useState<Date>(moment().toDate())

    const [errorBookmark, setErrorBookmark] = useState()
    const [completedBookmark, setCompletedBookmark] = useState()
    // const [startingBookmark, setStartingBookmark] = useState()
    const [runningBookmark, setRunningBookmark] = useState()
    const [haltingBookmark, setHaltingBookmark] = useState()
    const [haltedBookmark, setHaltedBookmark] = useState()

    const [type, setType] = useState('')
    const [results, setResults] = useState<any[]>([])

    const fetchData = async(type:string, paginate=false, useLimit=true ) => {
        let bookmark:string|undefined
        if(type === "ERROR") bookmark = errorBookmark
        if(type === "COMPLETED") bookmark = completedBookmark
        // if(type === "STARTING") bookmark = startingBookmark
        if(type === "RUNNING") bookmark = runningBookmark
        if(type === "HALTING") bookmark = haltingBookmark
        if(type === "HALTED") bookmark = haltedBookmark

        const filters:any = { 
            limit:useLimit? limit: allLimit}
        // if(prefix?.trim()) filters['prefix'] = prefix.trim().toLocaleLowerCase()
        if(paginate && bookmark) filters['bookmark'] = bookmark
        if(paginate && !bookmark) return {status:'done'}
        const res = await fetch('/api/search/wfRun',{
            method:'POST',
            body: JSON.stringify({
                
                statusAndSpec:{
                    status: type,
                    wfSpecName: id,
                    wfSpecVersion: version,
                    earliestStart:startDt,
                    latestStart:endDt
                },
                ...filters
            }),
        })
        if(res.ok){
            const response = await res.json()
            return {...response, status:'ok'}
        }
    }
    const getData = async () => {
        setLoading(true)
        const {results, bookmark} = await fetchData(type)
        if(type === "ERROR") setErrorBookmark(bookmark)
        if(type === "COMPLETED") setCompletedBookmark(bookmark)
        // if(type === "STARTING") setStartingBookmark(bookmark)
        if(type === "RUNNING") setRunningBookmark(bookmark)
        if(type === "HALTING") setHaltingBookmark(bookmark)
        if(type === "HALTED") setHaltedBookmark(bookmark)
        setResults(results.map( (v:Result) => ({...v, status:type})))
        setLoading(false)
    }
    const getMData = async () => {
        setErrorBookmark(undefined)
        setCompletedBookmark(undefined)
        // setStartingBookmark(undefined)
        setRunningBookmark(undefined)
        setHaltingBookmark(undefined)
        setHaltedBookmark(undefined)
        if(type) return getData()

        setLoading(true)
        setResults([])

        // const starting = await fetchData('STARTING', false, false)
        // setStartingBookmark(starting.bookmark)
        // setResults(prev => [...prev, ...starting.results.map((v:any) => ({...v, status:'STARTING'}))])

        const running = await fetchData('RUNNING', false, false)
        setRunningBookmark(running.bookmark)
        setResults(prev => [...prev, ...running.results.map((v:any) => ({...v, status:'RUNNING'}))])

        const completed = await fetchData('COMPLETED', false, false)
        setCompletedBookmark(completed.bookmark)
        setResults(prev => [...prev, ...completed.results.map((v:any) => ({...v, status:'COMPLETED'}))])

        const errors = await fetchData('ERROR', false, false)
        setErrorBookmark(errors.bookmark)
        setResults(prev => [...prev, ...errors.results.map((v:any) => ({...v, status:'ERROR'}))])

        const haltings = await fetchData('HALTING', false, false)
        setHaltingBookmark(haltings.bookmark)
        setResults(prev => [...prev, ...haltings.results.map((v:any) => ({...v, status:'HALTING'}))])
        
        const halteds = await fetchData('HALTED', false, false)
        setHaltedBookmark(halteds.bookmark)
        setResults(prev => [...prev, ...halteds.results.map((v:any) => ({...v, status:'HALTED'}))])

        setFirstLoad(true)
        setLoading(false)
    }
    const loadMMore = async () => {
        if(type) return loadMore()
        setLoading(true)

        

        // if(startingBookmark){
        //     const starting = await fetchData('STARTING', true, false)
        //     if(starting.status!='done'){
        //         setStartingBookmark(starting.bookmark)
        //         setResults(prev => [...prev, ...starting.results.map((v:any) => ({...v, status:'STARTING'}))])
        //     }
        // }
        if(runningBookmark){
            const running = await fetchData('RUNNING', true, false)
            if(running.status!='done'){
                setRunningBookmark(running.bookmark)
                setResults(prev => [...prev, ...running.results.map((v:any) => ({...v, status:'RUNNING'}))])
            }
        }
        if(completedBookmark){
            const completed = await fetchData('COMPLETED', true, false)
            if(completed.status!='done'){
                setCompletedBookmark(completed.bookmark)
                setResults(prev => [...prev, ...completed.results.map((v:any) => ({...v, status:'COMPLETED'}))])
            }
        }
        if(errorBookmark){
            const tasks = await fetchData('ERROR', true, false)
            if(tasks.status!='done'){
                setErrorBookmark(tasks.bookmark)
                setResults(prev => [...prev, ...tasks.results.map((v:any) => ({...v, status:'ERROR'}))])
            } 
        }
        if(haltingBookmark){
            const haltings = await fetchData('HALTING', true, false)
            if(haltings.status!='done'){
                setHaltingBookmark(haltings.bookmark)
                setResults(prev => [...prev, ...haltings.results.map((v:any) => ({...v, status:'HALTING'}))])
            } 
        }
        if(haltedBookmark){
            const halteds = await fetchData('HALTED', true, false)
            if(halteds.status!='done'){
                setHaltedBookmark(halteds.bookmark)
                setResults(prev => [...prev, ...halteds.results.map((v:any) => ({...v, status:'HALTED'}))])
            } 
        }

        setLoading(false)
    }
    const loadMore = async () => {
        setLoading(true)

        const {results, bookmark, status} = await fetchData(type,true)
        
        if(status==='done') return 
        
        if(type === "ERROR") setErrorBookmark(bookmark)
        if(type === "COMPLETED") setCompletedBookmark(bookmark)
        // if(type === "STARTING") setStartingBookmark(bookmark)
        if(type === "RUNNING") setRunningBookmark(bookmark)
        if(type === "HALTING") setHaltingBookmark(bookmark)
        if(type === "HALTED") setHaltedBookmark(bookmark)
        setResults(prev => [...prev, ...results.map((v:any) => ({...v, status:type}))])
        setLoading(false)
    }

    // const keyDownHandler = (e:React.KeyboardEvent<HTMLInputElement>) => {
    //     clearTimeout(myTimeout)
    //     if( e.key == 'Enter' ) return getMData()
    //     myTimeout = setTimeout(getMData, keyDownDelay);
    // }

    useEffect( () => {
        if(firstLoad) getMData()
    },[type])
    useEffect( () => {
        if(firstLoad) getMData()
    },[startDt, endDt])

    useEffect( () => {
        if(!first) return
        first = false
        getMData()
    },[])
    return <section>
        
        <div className="between">
            <h2>TaskRun search</h2> 
            <div className="btns btns-right">
                <CalendarB
                    changeEarlyDate={setStartDT} earlyDate={startDt}
                    changeLastDate={setEndDT} lastDate={endDt}
                />
                <Label>STATUS:</Label>
                <Button active={type === ''} onClick={() => setType("")}>All</Button>
                {/* <Button active={type === 'STARTING'} onClick={() => setType("STARTING")}>Starting</Button> */}
                <Button active={type === 'RUNNING'} onClick={() => setType("RUNNING")}>Running</Button>
                <Button active={type === 'COMPLETED'} onClick={() => setType("COMPLETED")}>Completed</Button>
                <Button active={type === 'ERROR'} onClick={() => setType("ERROR")}>Error</Button>
                <Button active={type === 'HALTING'} onClick={() => setType("HALTING")}>Halting</Button>
                <Button active={type === 'HALTED'} onClick={() => setType("HALTED")}>Halted</Button>
            </div>
        </div>

        <WfRunSearchTable results={results} />
        
        <div className="end">
            <div className="btns btns-right">
            {!!type ? <><Label>Rows per load:</Label><PerPage icon="/expand_more.svg" value={limit} onChange={setLimit} values={[10,20,30,60,100]} /> </>: undefined}
                <LoadMoreButton loading={loading} disabled={!haltedBookmark && !haltingBookmark && !runningBookmark && !errorBookmark && !completedBookmark} onClick={loadMMore}>Load More</LoadMoreButton>
            </div>
        </div>

    </section>

}