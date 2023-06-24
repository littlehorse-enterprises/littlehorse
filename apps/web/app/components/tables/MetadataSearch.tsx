import { useEffect, useState } from "react"
import { Button, LoadMoreButton } from "ui"

interface Result{
    name:string 
    version?:number 
    type?:string
}
const allLimit = 10

let first = true
export const MetadataSearch = () => {
    const [loading, setLoading] = useState(false)
    const [firstLoad, setFirstLoad] = useState(false)
    const [limit, setLimit] = useState(10)
    const [taskDefBookmark, setTaskDefBookmark] = useState()
    const [wfSpecBookmark, setWfSpecBookmark] = useState()
    const [bookmark, setExternalEventDefBookmark] = useState()
    const [type, setType] = useState('')
    const [prefix, setPrefix] = useState('')
    const [results, setResults] = useState<any[]>([])

    const fetchData = async(kind:string, paginate=false, useLimit=true ) => {
        const filters:any = { limit:useLimit? limit: allLimit }
        if(prefix) filters['prefix'] = prefix
        if(paginate && bookmark) filters['bookmark'] = bookmark
        if(paginate && !bookmark) return {status:'done'}
        const res = await fetch('./api/search/'+kind,{
            method:'POST',
            body: JSON.stringify({
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
        if(type === "wfSpec") setWfSpecBookmark(bookmark)
        if(type === "taskDef") setTaskDefBookmark(bookmark)
        if(type === "externalEventDef") setExternalEventDefBookmark(bookmark)

        setResults(results.map( (v:Result) => ({...v, type:type.charAt(0).toUpperCase() + type.slice(1)})))
        setLoading(false)
    }
    const getMData = async () => {
        if(type) return getData()

        setLoading(true)
        setResults([])
        const wfSpecs = await fetchData('wfSpec', false, false)
        setWfSpecBookmark(wfSpecs.bookmark)
        setResults(prev => [...prev, ...wfSpecs.results.map((v:any) => ({...v, type:'WfSpecs'}))])

        const taskDefs = await fetchData('taskDef', false, false)
        setTaskDefBookmark(taskDefs.bookmark)
        setResults(prev => [...prev, ...taskDefs.results.map((v:any) => ({...v, type:'TaskDef'}))])

        const externalEventDefs = await fetchData('externalEventDef', false, false)
        setExternalEventDefBookmark(externalEventDefs.bookmark)
        setResults(prev => [...prev, ...externalEventDefs.results.map((v:any) => ({...v, type:'ExternalEventDef'}))])
        setFirstLoad(true)
        setLoading(false)
    }
    const loadMore = async () => {
        setLoading(true)
        const {results, bookmark, status} = await fetchData('wfSpec',true)
        if(status==='done') return 
        setExternalEventDefBookmark(bookmark)
        setResults(prev => [...prev, ...results])
        setLoading(false)
    }


    useEffect( () => {
        if(firstLoad) getMData()
        
    },[type])

    useEffect( () => {
        if(!first) return
        first = false
        getMData()
    },[])

    return <div>
        Metadata Search 
        <input placeholder="search" type="text" value={prefix} onChange={e => setPrefix(e.target.value)} />
        <select value={type} onChange={e => setType(e.target.value)}>
            <option value="">All</option>
            <option value="wfSpec">WfDef</option>
            <option value="taskDef">TaskDef</option>
            <option value="externalEventDef">ExternalEventDef</option>
        </select>
        <Button onClick={getMData}>Get Data</Button>
        {/* {bookmark} */}
        {results.map( r => <div key={r.name+''+r.version}>{r.name}: {r.version}: {r.type}</div>)}
        <LoadMoreButton loading={loading} disabled={!bookmark} onClick={loadMore}>Load More</LoadMoreButton>
        {!!type ? <input placeholder="limit" type="number" value={limit} onChange={e => setLimit(+e.target.value)} /> : undefined}
    </div>
}

