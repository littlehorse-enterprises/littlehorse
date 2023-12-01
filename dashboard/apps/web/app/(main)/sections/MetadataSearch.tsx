'use client'
import { Button, Input, Label, LoadMoreButton, Loader, PerPage } from 'ui'
import { useEffect, useState } from 'react'
import { MetadataSearchTable } from '../components/search/MetadataSearchTable'

export interface Result {
    name: string
    version?: number
    type?: string
}

const allLimit = 5
const defaultLimit = 10
const keyDownDelay = 1000 // miliseconds

let myTimeout: NodeJS.Timeout

export function MetadataSearch() {
    let first = true

    const [ loading, setLoading ] = useState(false)
    const [ firstLoad, setFirstLoad ] = useState(false)
    const [ limit, setLimit ] = useState(defaultLimit)
    const [ userTaskDefBookmark, setUserTaskDefBookmark ] = useState()
    const [ taskDefBookmark, setTaskDefBookmark ] = useState()
    const [ wfSpecBookmark, setWfSpecBookmark ] = useState()
    const [ externalEventDefBookmark, setExternalEventDefBookmark ] = useState()
    const [ metadataType, setMetadataType ] = useState('')
    const [ prefix, setPrefix ] = useState('')
    const [ metadataResults, setMetadataResults ] = useState<any[]>([])

    const fetchData = async (type: string, paginate = false, useLimit = true) => {
        let bookmark: string | undefined
        if (type === 'wfSpec') {bookmark = wfSpecBookmark}
        if (type === 'taskDef') {bookmark = taskDefBookmark}
        if (type === 'userTaskDef') {bookmark = userTaskDefBookmark}
        if (type === 'externalEventDef') {bookmark = externalEventDefBookmark}
        const filters: any = { limit: useLimit ? limit : allLimit }
        if (prefix?.trim()) {filters.prefix = prefix.trim().toLocaleLowerCase()}
        if (paginate && bookmark) {filters.bookmark = bookmark}
        if (paginate && !bookmark) {return { status: 'done' }}
        const res = await fetch(`./api/search/${type}`, {
            method: 'POST',
            body: JSON.stringify({
                ...filters
            }),
        })
        if (res.ok) {
            const response = await res.json()
            return { ...response, status: 'ok' }
        }
    }
    const getData = async () => {
        setLoading(true)
        const { results, bookmark } = await fetchData(metadataType)
        if (metadataType === 'wfSpec') {setWfSpecBookmark(bookmark)}
        if (metadataType === 'taskDef') {setTaskDefBookmark(bookmark)}
        if (metadataType === 'userTaskDef') {setUserTaskDefBookmark(bookmark)}
        if (metadataType === 'externalEventDef') {setExternalEventDefBookmark(bookmark)}
        setMetadataResults(results.map((v: Result) => ({ ...v, type: metadataType.charAt(0).toUpperCase() + metadataType.slice(1) })))
        setLoading(false)
    }
    const getMData = async () => {
        setWfSpecBookmark(undefined)
        setTaskDefBookmark(undefined)
        setUserTaskDefBookmark(undefined)
        setExternalEventDefBookmark(undefined)
        if (metadataType) {return getData()}

        setLoading(true)
        // setResults([])

        const wfSpecs = await fetchData('wfSpec', false, false)
        if (wfSpecs !== undefined && wfSpecs.results !== undefined) {
            setWfSpecBookmark(wfSpecs.bookmark)
            setMetadataResults(_ => wfSpecs.results.map((v: any) => ({ ...v, type: 'WfSpec' })))
        }

        // setResults(prev => [...prev, ...wfSpecs.metadataResults.map((v:any) => ({...v, metadataType:'WfSpec'}))])

        const taskDefs = await fetchData('taskDef', false, false)
        if (taskDefs !== undefined && taskDefs.results !== undefined) {

            setTaskDefBookmark(taskDefs.bookmark)
            setMetadataResults(prev => [ ...prev, ...taskDefs.results.map((v: any) => ({ ...v, type: 'TaskDef' })) ])
        }

        const userTaskDefs = await fetchData('userTaskDef', false, false)
        if (userTaskDefs !== undefined && userTaskDefs.results !== undefined) {


            setTaskDefBookmark(userTaskDefs.bookmark)
            setMetadataResults(prev => [ ...prev, ...userTaskDefs.results.map((v: any) => ({ ...v, type: 'UserTaskDef' })) ])
        }


        const externalEventDefs = await fetchData('externalEventDef', false, false)
        if (externalEventDefs !== undefined && externalEventDefs.results !== undefined) {

            setExternalEventDefBookmark(externalEventDefs.bookmark)
            setMetadataResults(prev => [ ...prev, ...externalEventDefs.results.map((v: any) => ({ ...v, type: 'ExternalEventDef' })) ])
        }

        setFirstLoad(true)
        setLoading(false)
    }
    const loadMMore = async () => {
        if (metadataType) {return loadMore()}
        setLoading(true)

        if (wfSpecBookmark) {
            const wfSpecs = await fetchData('wfSpec', true, false)
            if (wfSpecs.status !== 'done') {
                setWfSpecBookmark(wfSpecs?.bookmark)
                setMetadataResults(prev => [ ...prev, ...wfSpecs.results.map((v: any) => ({ ...v, type: 'WfSpec' })) ])
            }
        }

        if (taskDefBookmark) {
            const taskDefs = await fetchData('taskDef', true, false)
            if (taskDefs.status !== 'done') {
                if (taskDefs.results !== undefined) {
                    setTaskDefBookmark(taskDefs.bookmark)
                    setMetadataResults(prev => [ ...prev, ...taskDefs.results.map((v: any) => ({ ...v, type: 'TaskDef' })) ])
                }
            }
        }
        if (userTaskDefBookmark) {
            const userTaskDefs = await fetchData('userTaskDef', true, false)
            if (userTaskDefs.status !== 'done') {
                if (userTaskDefs.results !== undefined) {
                    setTaskDefBookmark(userTaskDefs.bookmark)
                    setMetadataResults(prev => [ ...prev, ...userTaskDefs.results.map((v: any) => ({ ...v, type: 'UserTaskDef' })) ])
                }
            }
        }
        if (externalEventDefBookmark) {
            const externalEventDefs = await fetchData('externalEventDef', true, false)
            if (externalEventDefs.status !== 'done') {
                if (externalEventDefs.results !== undefined) {
                    setExternalEventDefBookmark(externalEventDefs.bookmark)
                    setMetadataResults(prev => [ ...prev, ...externalEventDefs.results.map((v: any) => ({
                        ...v,
                        type: 'ExternalEventDef'
                    })) ])
                }
            }
        }

        setLoading(false)
    }
    const loadMore = async () => {
        setLoading(true)

        const { results, bookmark, status } = await fetchData(metadataType, true)

        if (status === 'done') {return}

        if (metadataType === 'wfSpec') {setWfSpecBookmark(bookmark)}
        if (metadataType === 'taskDef') {setTaskDefBookmark(bookmark)}
        if (metadataType === 'userTaskDef') {setUserTaskDefBookmark(bookmark)}
        if (metadataType === 'externalEventDef') {setExternalEventDefBookmark(bookmark)}
        setMetadataResults(prev => [ ...prev, ...results.map((v: any) => ({
            ...v,
            type: metadataType.charAt(0).toUpperCase() + metadataType.slice(1)
        })) ])
        setLoading(false)
    }

    const keyDownHandler = (e: React.KeyboardEvent<HTMLInputElement>) => {
        clearTimeout(myTimeout)
        if (e.key === 'Enter') {return getMData()}
        myTimeout = setTimeout(getMData, keyDownDelay)
    }

    useEffect(() => {
        if (firstLoad) {getMData()}
    }, [ metadataType ])

    useEffect(() => {
        if (!first) {return}
        first = false
        getMData()
    }, [])
    return <section>

        <div className="between">
            <h2>Metadata search</h2>
            <div className="btns btns-right">
                <Input icon="/search.svg" onChange={e => { setPrefix(e.target.value) }} onKeyDown={keyDownHandler} placeholder="Search by name or ID"
                    type="text" value={prefix}/>
                <Label>TYPE:</Label>
                <Button active={metadataType === ''} onClick={() => { setMetadataType('') }}>All</Button>
                <Button active={metadataType === 'wfSpec'} onClick={() => { setMetadataType('wfSpec') }}>WfSpec</Button>
                <Button active={metadataType === 'taskDef'} onClick={() => { setMetadataType('taskDef') }}>TaskDef</Button>
                <Button active={metadataType === 'userTaskDef'} onClick={() => { setMetadataType('userTaskDef') }}>UserTaskDef</Button>
                <Button active={metadataType === 'externalEventDef'}
                    onClick={() => { setMetadataType('externalEventDef') }}>ExternalEventDef</Button>
            </div>
        </div>
        <div className={`${metadataResults.length === 0 ? 'flex items-center justify-items-center justify-center' : ''}`}
            style={{ minHeight: '568px' }}
        >
            {metadataResults.length ? (
                <MetadataSearchTable results={metadataResults}/>
            ) : (
                <Loader/>
            )}
        </div>

        <div className="end">
            <div className="btns btns-right">
                {metadataType ? <><Label>Rows per load:</Label><PerPage icon="/expand_more.svg" onChange={setLimit}
                    value={limit}
                    values={[ 10, 20, 30, 60, 100 ]}/> </> : undefined}
                <LoadMoreButton disabled={!externalEventDefBookmark && !wfSpecBookmark && !taskDefBookmark}
                    loading={loading}
                    onClick={loadMMore}>Load More</LoadMoreButton>
            </div>
        </div>

    </section>

}
