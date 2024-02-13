'use client'
import {
    Button,
    CalendarB,
    Label,
    LoadMoreButton,
    Loader,
    PerPage
} from 'ui'
import React, { useEffect, useState } from 'react'
import moment from 'moment'
import { WfRunSearchTable } from '../components/search/WfRunSearchTable'
import { getVersionFromFormattedString } from '../components/common/VersionExtractor'

export interface Result {
    id: string
    status?: string
}

const allLimit = 5
const defaultLimit = 15

export function WfRunSearch({ id, version }: any) {
    let first = true

    const [ loading, setLoading ] = useState(false)
    const [ firstLoad, setFirstLoad ] = useState(false)
    const [ limit, setLimit ] = useState(defaultLimit)

    const [ startDt, setStartDt ] = useState<Date>(moment().startOf('day').toDate())
    const [ endDt, setEndDt ] = useState<Date>(moment().toDate())

    const [ errorBookmark, setErrorBookmark ] = useState()
    const [ completedBookmark, setCompletedBookmark ] = useState()
    // const [startingBookmark, setStartingBookmark] = useState()
    const [ runningBookmark, setRunningBookmark ] = useState()
    const [ haltingBookmark, setHaltingBookmark ] = useState()
    const [ haltedBookmark, setHaltedBookmark ] = useState()

    const [ type, setType ] = useState('')
    const [ wfRunSearchResults, setWfRunSearchResults ] = useState<any[]>([])

    const fetchData = async (wfRunStatus: string, paginate = false, useLimit = true) => {
        let bookmark: string | undefined
        if (wfRunStatus === 'ERROR') {bookmark = errorBookmark}
        if (wfRunStatus === 'COMPLETED') {bookmark = completedBookmark}
        // if(wfRunStatus === "STARTING") bookmark = startingBookmark
        if (wfRunStatus === 'RUNNING') {bookmark = runningBookmark}
        if (wfRunStatus === 'HALTING') {bookmark = haltingBookmark}
        if (wfRunStatus === 'HALTED') {bookmark = haltedBookmark}

        const filters: any = {
            limit: useLimit ? limit : allLimit
        }
        // if(prefix?.trim()) filters['prefix'] = prefix.trim().toLocaleLowerCase()
        if (paginate && bookmark) {filters.bookmark = bookmark}
        if (paginate && !bookmark) {return { status: 'done' }}

        const { majorVersion, revision } = getVersionFromFormattedString(version)

        const res = await fetch('/api/search/wfRun', {
            method: 'POST',
            body: JSON.stringify({
                status: wfRunStatus,
                wfSpecName: id,
                wfSpecMajorVersion: majorVersion,
                wfSpecRevision: revision,
                earliestStart: startDt,
                latestStart: endDt,
                ...filters
            })
        })
        if (res.ok) {
            const response = await res.json()
            return { ...response, status: 'ok' }
        }
    }
    const getData = async () => {
        setLoading(true)
        const { results, bookmark } = await fetchData(type)
        if (type === 'ERROR') {setErrorBookmark(bookmark)}
        if (type === 'COMPLETED') {setCompletedBookmark(bookmark)}
        // if(type === "STARTING") setStartingBookmark(bookmark)
        if (type === 'RUNNING') {setRunningBookmark(bookmark)}
        if (type === 'HALTING') {setHaltingBookmark(bookmark)}
        if (type === 'HALTED') {setHaltedBookmark(bookmark)}
        setWfRunSearchResults(results.map((v: Result) => ({ ...v, status: type })))
        setLoading(false)
    }
    const getMData = async () => {
        setErrorBookmark(undefined)
        setCompletedBookmark(undefined)
        // setStartingBookmark(undefined)
        setRunningBookmark(undefined)
        setHaltingBookmark(undefined)
        setHaltedBookmark(undefined)
        if (type) {return getData()}

        setLoading(true)
        setWfRunSearchResults([])

        // const starting = await fetchData('STARTING', false, false)
        // setStartingBookmark(starting.bookmark)
        // setResults(prev => [...prev, ...starting.wfRunSearchResults.map((v:any) => ({...v, status:'STARTING'}))])

        const running = await fetchData('RUNNING', false, false)
        if (running !== undefined && running.results !== undefined) {
            setRunningBookmark(running.bookmark)
            setWfRunSearchResults(prev => [
                ...prev,
                ...running.results?.map((v: any) => ({ ...v, status: 'RUNNING' }))
            ])
        }

        const completed = await fetchData('COMPLETED', false, false)
        if (completed !== undefined && completed.results !== undefined) {


            setCompletedBookmark(completed.bookmark)
            setWfRunSearchResults(prev => [
                ...prev,
                ...completed.results.map((v: any) => ({ ...v, status: 'COMPLETED' }))
            ])
        }

        const errors = await fetchData('ERROR', false, false)
        if (errors !== undefined && errors.results !== undefined) {

            setErrorBookmark(errors.bookmark)
            setWfRunSearchResults(prev => [
                ...prev,
                ...errors.results.map((v: any) => ({ ...v, status: 'ERROR' }))
            ])
        }

        const haltings = await fetchData('HALTING', false, false)
        if (haltings !== undefined && haltings.results !== undefined) {

            setHaltingBookmark(haltings.bookmark)
            setWfRunSearchResults(prev => [
                ...prev,
                ...haltings.results.map((v: any) => ({ ...v, status: 'HALTING' }))
            ])
        }

        const halteds = await fetchData('HALTED', false, false)
        if (halteds !== undefined && halteds.results !== undefined) {


            setHaltedBookmark(halteds.bookmark)
            setWfRunSearchResults(prev => [
                ...prev,
                ...halteds.results.map((v: any) => ({ ...v, status: 'HALTED' }))
            ])
        }
        setFirstLoad(true)
        setLoading(false)
    }
    const loadMMore = async () => {
        if (type) {return loadMore()}
        setLoading(true)

        // if(startingBookmark){
        //     const starting = await fetchData('STARTING', true, false)
        //     if(starting.status!='done'){
        //         setStartingBookmark(starting.bookmark)
        //         setResults(prev => [...prev, ...starting.wfRunSearchResults.map((v:any) => ({...v, status:'STARTING'}))])
        //     }
        // }
        if (runningBookmark) {
            const running = await fetchData('RUNNING', true, false)
            if (running.status !== 'done') {
                setRunningBookmark(running.bookmark)
                setWfRunSearchResults(prev => [
                    ...prev,
                    ...running.results.map((v: any) => ({ ...v, status: 'RUNNING' }))
                ])
            }
        }
        if (completedBookmark) {
            const completed = await fetchData('COMPLETED', true, false)
            if (completed.status !== 'done') {
                setCompletedBookmark(completed.bookmark)
                setWfRunSearchResults(prev => [
                    ...prev,
                    ...completed.results.map((v: any) => ({ ...v, status: 'COMPLETED' }))
                ])
            }
        }
        if (errorBookmark) {
            const tasks = await fetchData('ERROR', true, false)
            if (tasks.status !== 'done') {
                setErrorBookmark(tasks.bookmark)
                setWfRunSearchResults(prev => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({ ...v, status: 'ERROR' }))
                ])
            }
        }
        if (haltingBookmark) {
            const haltings = await fetchData('HALTING', true, false)
            if (haltings.status !== 'done') {
                setHaltingBookmark(haltings.bookmark)
                setWfRunSearchResults(prev => [
                    ...prev,
                    ...haltings.results.map((v: any) => ({ ...v, status: 'HALTING' }))
                ])
            }
        }
        if (haltedBookmark) {
            const halteds = await fetchData('HALTED', true, false)
            if (halteds.status !== 'done') {
                setHaltedBookmark(halteds.bookmark)
                setWfRunSearchResults(prev => [
                    ...prev,
                    ...halteds.results.map((v: any) => ({ ...v, status: 'HALTED' }))
                ])
            }
        }

        setLoading(false)
    }
    const loadMore = async () => {
        setLoading(true)

        const { results, bookmark, status } = await fetchData(type, true)

        if (status === 'done') {return}

        if (type === 'ERROR') {setErrorBookmark(bookmark)}
        if (type === 'COMPLETED') {setCompletedBookmark(bookmark)}
        // if(type === "STARTING") setStartingBookmark(bookmark)
        if (type === 'RUNNING') {setRunningBookmark(bookmark)}
        if (type === 'HALTING') {setHaltingBookmark(bookmark)}
        if (type === 'HALTED') {setHaltedBookmark(bookmark)}
        setWfRunSearchResults(prev => [
            ...prev,
            ...results.map((v: any) => ({ ...v, status: type }))
        ])
        setLoading(false)
    }

    // const keyDownHandler = (e:React.KeyboardEvent<HTMLInputElement>) => {
    //     clearTimeout(myTimeout)
    //     if( e.key == 'Enter' ) return getMData()
    //     myTimeout = setTimeout(getMData, keyDownDelay);
    // }

    useEffect(() => {
        if (firstLoad) {getMData()}
    }, [ type ])
    useEffect(() => {
        if (firstLoad) {getMData()}
    }, [ startDt, endDt ])

    useEffect(() => {
        if (!first) {return}
        first = false
        getMData()
    }, [])
    return (
        <section>
            <div className='between'>
                <h2>WfRun search</h2>
                <div className='btns btns-right'>
                    <CalendarB
                        changeEarlyDate={setStartDt}
                        changeLastDate={setEndDt}
                        earlyDate={startDt}
                        lastDate={endDt}
                    />
                    <Label>STATUS:</Label>
                    <Button active={type === ''} onClick={() => { setType('') }}>
                        All
                    </Button>
                    {/* <Button active={type === 'STARTING'} onClick={() => setType("STARTING")}>Starting</Button> */}
                    <Button
                        active={type === 'RUNNING'}
                        onClick={() => { setType('RUNNING') }}
                    >
                        Running
                    </Button>
                    <Button
                        active={type === 'COMPLETED'}
                        onClick={() => { setType('COMPLETED') }}
                    >
                        Completed
                    </Button>
                    <Button active={type === 'ERROR'} onClick={() => { setType('ERROR') }}>
                        Error
                    </Button>
                    <Button
                        active={type === 'HALTING'}
                        onClick={() => { setType('HALTING') }}
                    >
                        Halting
                    </Button>
                    <Button active={type === 'HALTED'} onClick={() => { setType('HALTED') }}>
                        Halted
                    </Button>
                </div>
            </div>
            <div
                className={`${
                    wfRunSearchResults.length === 0
                        ? 'flex items-center justify-items-center justify-center'
                        : ''
                }`}
                style={{ minHeight: '568px' }}
            >
                {wfRunSearchResults.length > 0 ? (
                    <WfRunSearchTable results={wfRunSearchResults} wfspec={id} />
                ) : (
                    <Loader />
                )}
            </div>

            <div className='end'>
                <div className='btns btns-right'>
                    {type ? (
                        <>
                            <Label>Rows per load:</Label>
                            <PerPage
                                icon='/expand_more.svg'
                                onChange={setLimit}
                                value={limit}
                                values={[ 10, 20, 30, 60, 100 ]}
                            />{' '}
                        </>
                    ) : undefined}
                    <LoadMoreButton
                        disabled={
                            !haltedBookmark &&
							!haltingBookmark &&
							!runningBookmark &&
							!errorBookmark &&
							!completedBookmark
                        }
                        loading={loading}
                        onClick={loadMMore}
                    >
                        Load More
                    </LoadMoreButton>
                </div>
            </div>
        </section>
    )
}
