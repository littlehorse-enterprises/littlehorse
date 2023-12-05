'use client'
import {
    Button,
    CalendarB,
    Input,
    Label,
    LoadMoreButton,
    Loader,
    PerPage,
} from 'ui'
import { useEffect, useState } from 'react'
import moment from 'moment'
import { UserTaskRunSearchTable } from '../search/userTaskRunSearchTable'
import type { WfRunId } from '../../../../../littlehorse-public-api/object_id'

export interface Result {
    id: any;
    wfRunId: WfRunId;
    userTaskGuid: string;
    status?: string;
}

const allLimit = 5
const defaultLimit = 15
const keyDownDelay = 1000 // miliseconds

let myTimeout: NodeJS.Timeout

export function UserTaskRunSearch({ id }: any) {
    let first = true

    const [ userId, setUserId ] = useState('')
    const keyDownHandler = (e: React.KeyboardEvent<HTMLInputElement>) => {
        clearTimeout(myTimeout)
        if (e.key === 'Enter') {return getMData()}
        myTimeout = setTimeout(getMData, keyDownDelay)
    }

    const [ loading, setLoading ] = useState(false)
    const [ firstLoad, setFirstLoad ] = useState(false)
    const [ limit, setLimit ] = useState(defaultLimit)

    const [ startDt, setStartDt ] = useState<Date>(
        moment().startOf('day').toDate()
    )
    const [ endDt, setEndDt ] = useState<Date>(moment().toDate())
    const [ assignedBookmark, setAssignedBookmark ] = useState()
    const [ unAssignedBookmark, setUnAssignedBookmark ] = useState()
    const [ doneBookmark, setDoneBookmark ] = useState()
    const [ cancelledBookmark, setCancelledBookmark ] = useState()

    const [ type, setType ] = useState('')
    const [ userTaskSearchResults, setUserTaskSearchResults ] = useState<any[]>([])

    const fetchData = async (
        userTaskStatus: string,
        paginate = false,
        useLimit = true
    ) => {
        let bookmark: string | undefined
        if (userTaskStatus === 'ASSIGNED') {bookmark = assignedBookmark}
        if (userTaskStatus === 'UNASSIGNED') {bookmark = unAssignedBookmark}
        if (userTaskStatus === 'DONE') {bookmark = doneBookmark}
        if (userTaskStatus === 'CANCELLED') {bookmark = cancelledBookmark}

        const filters: any = {
            limit: useLimit ? limit : allLimit,
        }
        if (paginate && bookmark) {filters.bookmark = bookmark}
        if (paginate && !bookmark) {return { status: 'done' }}
        if (userId) {filters.user = { id: userId }}

        const res = await fetch('/api/search/userTaskRun', {
            method: 'POST',
            body: JSON.stringify({
                status: userTaskStatus,
                userTaskDefName: id,
                // userGroup: "string",
                earliestStart: startDt,
                latestStart: endDt,

                ...filters,
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
        if (type === 'ASSIGNED') {setAssignedBookmark(bookmark)}
        if (type === 'UNASSIGNED') {setUnAssignedBookmark(bookmark)}
        if (type === 'DONE') {setDoneBookmark(bookmark)}
        if (type === 'CANCELLED') {setCancelledBookmark(bookmark)}

        setUserTaskSearchResults(results.map((v: Result) => ({ ...v, status: type })))
        setLoading(false)
    }
    const getMData = async () => {
        setAssignedBookmark(undefined)
        setUnAssignedBookmark(undefined)
        setDoneBookmark(undefined)
        setCancelledBookmark(undefined)
        if (type) {return getData()}

        setLoading(true)
        setUserTaskSearchResults([])

        const assigned = await fetchData('ASSIGNED', false, false)
        setAssignedBookmark(assigned.bookmark)
        setUserTaskSearchResults((prev) => [
            ...prev,
            ...assigned.results?.map((v: any) => ({
                ...v,
                status: 'ASSIGNED',
            })) || {},
        ])

        const unassigned = await fetchData('UNASSIGNED', false, false)
        setUnAssignedBookmark(unassigned.bookmark)
        setUserTaskSearchResults((prev) => [
            ...prev,
            ...unassigned.results?.map((v: any) => ({
                ...v,
                status: 'UNASSIGNED',
            })) || {},
        ])

        const done = await fetchData('DONE', false, false)
        setDoneBookmark(done.bookmark)
        setUserTaskSearchResults((prev) => [
            ...prev,
            ...done.results?.map((v: any) => ({
                ...v,
                status: 'DONE',
            })) || {},
        ])

        const cancelled = await fetchData('CANCELLED', false, false)
        setCancelledBookmark(cancelled.bookmark)
        setUserTaskSearchResults((prev) => [
            ...prev,
            ...cancelled.results?.map((v: any) => ({
                ...v,
                status: 'CANCELLED',
            })) || {},
        ])

        setFirstLoad(true)
        setLoading(false)
    }
    const loadMMore = async () => {
        if (type) {return loadMore()}
        setLoading(true)

        if (assignedBookmark) {
            const tasks = await fetchData('ASSIGNED', true, false)
            if (tasks.status !== 'done') {
                setAssignedBookmark(tasks.bookmark)
                setUserTaskSearchResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: 'ASSIGNED',
                    })),
                ])
            }
        }

        if (unAssignedBookmark) {
            const tasks = await fetchData('UNASSIGNED', true, false)
            if (tasks.status !== 'done') {
                setUnAssignedBookmark(tasks.bookmark)
                setUserTaskSearchResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: 'UNASSIGNED',
                    })),
                ])
            }
        }

        if (doneBookmark) {
            const tasks = await fetchData('DONE', true, false)
            if (tasks.status !== 'done') {
                setDoneBookmark(tasks.bookmark)
                setUserTaskSearchResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: 'DONE',
                    })),
                ])
            }
        }

        if (cancelledBookmark) {
            const tasks = await fetchData('CANCELLED', true, false)
            if (tasks.status !== 'done') {
                setCancelledBookmark(tasks.bookmark)
                setUserTaskSearchResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: 'CANCELLED',
                    })),
                ])
            }
        }

        setLoading(false)
    }
    const loadMore = async () => {
        setLoading(true)

        const { results, bookmark, status } = await fetchData(type, true)

        if (status === 'done') {return}

        if (type === 'ASSIGNED') {setAssignedBookmark(bookmark)}
        if (type === 'UNASSIGNED') {setUnAssignedBookmark(bookmark)}
        if (type === 'DONE') {setDoneBookmark(bookmark)}
        if (type === 'CANCELLED') {setCancelledBookmark(bookmark)}

        setUserTaskSearchResults((prev) => [
            ...prev,
            ...results.map((v: any) => ({ ...v, status: type })),
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
            <h2>UserTaskRun Search</h2>
            <Input
                icon="/search.svg"
                onChange={(e) => { setUserId(e.target.value) }}
                onKeyDown={keyDownHandler}
                placeholder="Search by assigned User ID or User Group"
                type="text"
                value={userId}
            />
            <div className="between">
                <div className="btns btns-right">
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
                    <Button
                        active={type === 'ASSIGNED'}
                        onClick={() => { setType('ASSIGNED') }}
                    >
                        Assigned
                    </Button>
                    <Button
                        active={type === 'UNASSIGNED'}
                        onClick={() => { setType('UNASSIGNED') }}
                    >
                        Unassigned
                    </Button>
                    <Button
                        active={type === 'DONE'}
                        onClick={() => { setType('DONE') }}
                    >
                        Done
                    </Button>
                    <Button
                        active={type === 'CANCELLED'}
                        onClick={() => { setType('CANCELLED') }}
                    >
                        Cancelled
                    </Button>
                </div>
            </div>
            <div
                className={`${
                    userTaskSearchResults.length === 0
                        ? 'flex items-center justify-items-center justify-center'
                        : ''
                }`}
                style={{ minHeight: '568px' }}
            >
                {userTaskSearchResults.length > 0 ? (
                    <UserTaskRunSearchTable results={userTaskSearchResults} wfspec={id} />
                ) : (
                    <Loader />
                )}
            </div>
            <div className="end">
                <div className="btns btns-right">
                    {type ? (
                        <>
                            <Label>Rows per load:</Label>
                            <PerPage
                                icon="/expand_more.svg"
                                onChange={setLimit}
                                value={limit}
                                values={[ 10, 20, 30, 60, 100 ]}
                            />{' '}
                        </>
                    ) : undefined}
                    <LoadMoreButton
                        disabled={
                            !assignedBookmark &&
                            !unAssignedBookmark &&
                            !doneBookmark &&
                            !cancelledBookmark
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
