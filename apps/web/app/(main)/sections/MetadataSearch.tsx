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
  const [ type, setType ] = useState('')
  const [ prefix, setPrefix ] = useState('')
  const [ results, setResults ] = useState<any[]>([])

  const fetchData = async (type: string, paginate = false, useLimit = true) => {
    let bookmark: string | undefined
    if (type === 'wfSpec') bookmark = wfSpecBookmark
    if (type === 'taskDef') bookmark = taskDefBookmark
    if (type === 'userTaskDef') bookmark = userTaskDefBookmark
    if (type === 'externalEventDef') bookmark = externalEventDefBookmark
    const filters: any = { limit: useLimit ? limit : allLimit }
    if (prefix?.trim()) filters.prefix = prefix.trim().toLocaleLowerCase()
    if (paginate && bookmark) filters.bookmark = bookmark
    if (paginate && !bookmark) return { status: 'done' }
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
    const { results, bookmark } = await fetchData(type)
    if (type === 'wfSpec') setWfSpecBookmark(bookmark)
    if (type === 'taskDef') setTaskDefBookmark(bookmark)
    if (type === 'userTaskDef') setUserTaskDefBookmark(bookmark)
    if (type === 'externalEventDef') setExternalEventDefBookmark(bookmark)
    setResults(results.map((v: Result) => ({ ...v, type: type.charAt(0).toUpperCase() + type.slice(1) })))
    setLoading(false)
  }
  const getMData = async () => {
    setWfSpecBookmark(undefined)
    setTaskDefBookmark(undefined)
    setUserTaskDefBookmark(undefined)
    setExternalEventDefBookmark(undefined)
    if (type) return getData()

    setLoading(true)
    // setResults([])

    const wfSpecs = await fetchData('wfSpec', false, false)
    if (wfSpecs.results !== undefined) {
      setWfSpecBookmark(wfSpecs.bookmark)
      setResults(prev => wfSpecs.results.map((v: any) => ({ ...v, type: 'WfSpec' })))
    }

    // setResults(prev => [...prev, ...wfSpecs.results.map((v:any) => ({...v, type:'WfSpec'}))])

    const taskDefs = await fetchData('taskDef', false, false)
    setTaskDefBookmark(taskDefs.bookmark)
    setResults(prev => [ ...prev, ...taskDefs.results.map((v: any) => ({ ...v, type: 'TaskDef' })) ])

    const userTaskDefs = await fetchData('userTaskDef', false, false)
    setTaskDefBookmark(userTaskDefs.bookmark)
    setResults(prev => [ ...prev, ...userTaskDefs.results.map((v: any) => ({ ...v, type: 'UserTaskDef' })) ])

    const externalEventDefs = await fetchData('externalEventDef', false, false)
    setExternalEventDefBookmark(externalEventDefs.bookmark)
    setResults(prev => [ ...prev, ...externalEventDefs.results.map((v: any) => ({ ...v, type: 'ExternalEventDef' })) ])

    setFirstLoad(true)
    setLoading(false)
  }
  const loadMMore = async () => {
    if (type) return loadMore()
    setLoading(true)

    if (wfSpecBookmark) {
      const wfSpecs = await fetchData('wfSpec', true, false)
      if (wfSpecs.status != 'done') {
        setWfSpecBookmark(wfSpecs?.bookmark)
        setResults(prev => [ ...prev, ...wfSpecs.results.map((v: any) => ({ ...v, type: 'WfSpec' })) ])
      }
    }

    if (taskDefBookmark) {
      const taskDefs = await fetchData('taskDef', true, false)
      if (taskDefs.status != 'done') {
        setTaskDefBookmark(taskDefs.bookmark)
        setResults(prev => [ ...prev, ...taskDefs.results.map((v: any) => ({ ...v, type: 'TaskDef' })) ])
      }
    }
    if (userTaskDefBookmark) {
      const userTaskDefs = await fetchData('userTaskDef', true, false)
      if (userTaskDefs.status != 'done') {
        setTaskDefBookmark(userTaskDefs.bookmark)
        setResults(prev => [ ...prev, ...userTaskDefs.results.map((v: any) => ({ ...v, type: 'UserTaskDef' })) ])
      }
    }
    if (externalEventDefBookmark) {
      const externalEventDefs = await fetchData('externalEventDef', true, false)
      if (externalEventDefs.status != 'done') {
        setExternalEventDefBookmark(externalEventDefs.bookmark)
        setResults(prev => [ ...prev, ...externalEventDefs.results.map((v: any) => ({
          ...v,
          type: 'ExternalEventDef'
        })) ])
      }
    }

    setLoading(false)
  }
  const loadMore = async () => {
    setLoading(true)

    const { results, bookmark, status } = await fetchData(type, true)

    if (status === 'done') return

    if (type === 'wfSpec') setWfSpecBookmark(bookmark)
    if (type === 'taskDef') setTaskDefBookmark(bookmark)
    if (type === 'userTaskDef') setUserTaskDefBookmark(bookmark)
    if (type === 'externalEventDef') setExternalEventDefBookmark(bookmark)
    setResults(prev => [ ...prev, ...results.map((v: any) => ({
      ...v,
      type: type.charAt(0).toUpperCase() + type.slice(1)
    })) ])
    setLoading(false)
  }

  const keyDownHandler = (e: React.KeyboardEvent<HTMLInputElement>) => {
    clearTimeout(myTimeout)
    if (e.key == 'Enter') return getMData()
    myTimeout = setTimeout(getMData, keyDownDelay)
  }

  useEffect(() => {
    if (firstLoad) getMData()
  }, [ type ])

  useEffect(() => {
    if (!first) return
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
        <Button active={type === ''} onClick={() => { setType('') }}>All</Button>
        <Button active={type === 'wfSpec'} onClick={() => { setType('wfSpec') }}>WfSpec</Button>
        <Button active={type === 'taskDef'} onClick={() => { setType('taskDef') }}>TaskDef</Button>
        <Button active={type === 'userTaskDef'} onClick={() => { setType('userTaskDef') }}>UserTaskDef</Button>
        <Button active={type === 'externalEventDef'}
          onClick={() => { setType('externalEventDef') }}>ExternalEventDef</Button>
      </div>
    </div>
    <div className={`${results.length === 0 ? 'flex items-center justify-items-center justify-center' : ''}`}
      style={{ minHeight: '568px' }}
    >
      {results.length ? (
        <MetadataSearchTable results={results}/>
      ) : (
        <Loader/>
      )}
    </div>

    <div className="end">
      <div className="btns btns-right">
        {type ? <><Label>Rows per load:</Label><PerPage icon="/expand_more.svg" onChange={setLimit}
          value={limit}
          values={[ 10, 20, 30, 60, 100 ]}/> </> : undefined}
        <LoadMoreButton disabled={!externalEventDefBookmark && !wfSpecBookmark && !taskDefBookmark}
          loading={loading}
          onClick={loadMMore}>Load More</LoadMoreButton>
      </div>
    </div>

  </section>

}