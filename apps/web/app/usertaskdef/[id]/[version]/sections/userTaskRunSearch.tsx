"use client";
import {
    Button,
    CalendarB,
    Input,
    Label,
    LoadMoreButton,
    Loader,
    PerPage,
} from "ui";
import { useEffect, useState } from "react";
import moment from "moment";
import { UserTaskRunSearchTable } from "../search/userTaskRunSearchTable";

export interface Result {
    id: any;
    wfRunId: string;
    userTaskGuid: string;
    status?: string;
}

const allLimit = 5;
const defaultLimit = 15;
const keyDownDelay = 1000; // miliseconds

let myTimeout: NodeJS.Timeout;

export const UserTaskRunSearch = ({ id }: any) => {
    let first = true;

    const [user_id, setUserId] = useState("");
    const keyDownHandler = (e: React.KeyboardEvent<HTMLInputElement>) => {
        clearTimeout(myTimeout);
        if (e.key == "Enter") return getMData();
        myTimeout = setTimeout(getMData, keyDownDelay);
    };

    const [loading, setLoading] = useState(false);
    const [firstLoad, setFirstLoad] = useState(false);
    const [limit, setLimit] = useState(defaultLimit);

    const [startDt, setStartDT] = useState<Date>(
        moment().startOf("day").toDate()
    );
    const [endDt, setEndDT] = useState<Date>(moment().toDate());
    const [assignedBookmark, setAssignedBookmark] = useState();
    const [unAssignedBookmark, setUnAssignedBookmark] = useState();
    const [doneBookmark, setDoneBookmark] = useState();
    const [cancelledBookmark, setCancelledBookmark] = useState();

    const [type, setType] = useState("");
    const [results, setResults] = useState<any[]>([]);

    const fetchData = async (
        type: string,
        paginate = false,
        useLimit = true
    ) => {
        let bookmark: string | undefined;
        if (type === "ASSIGNED") bookmark = assignedBookmark;
        if (type === "UNASSIGNED") bookmark = unAssignedBookmark;
        if (type === "DONE") bookmark = doneBookmark;
        if (type === "CANCELLED") bookmark = cancelledBookmark;

        const filters: any = {
            limit: useLimit ? limit : allLimit,
        };
        if (paginate && bookmark) filters["bookmark"] = bookmark;
        if (paginate && !bookmark) return { status: "done" };
        if (user_id) filters["user"] = { id: user_id };

        const res = await fetch("/api/search/userTaskRun", {
            method: "POST",
            body: JSON.stringify({
                status: type,
                userTaskDefName: id,
                // userGroup: "string",
                earliestStart: startDt,
                latestStart: endDt,

                ...filters,
            }),
        });
        if (res.ok) {
            const response = await res.json();
            console.log("response", response);
            return { ...response, status: "ok" };
        }
    };
    const getData = async () => {
        setLoading(true);
        const { results, bookmark } = await fetchData(type);
        if (type === "ASSIGNED") setAssignedBookmark(bookmark);
        if (type === "UNASSIGNED") setUnAssignedBookmark(bookmark);
        if (type === "DONE") setDoneBookmark(bookmark);
        if (type === "CANCELLED") setCancelledBookmark(bookmark);

        setResults(results.map((v: Result) => ({ ...v, status: type })));
        setLoading(false);
    };
    const getMData = async () => {
        setAssignedBookmark(undefined);
        setUnAssignedBookmark(undefined);
        setDoneBookmark(undefined);
        setCancelledBookmark(undefined);
        if (type) return getData();

        setLoading(true);
        setResults([]);

        const assigned = await fetchData("ASSIGNED", false, false);
        setAssignedBookmark(assigned.bookmark);
        setResults((prev) => [
            ...prev,
            ...assigned.results?.map((v: any) => ({
                ...v,
                status: "ASSIGNED",
            })),
        ]);

        const unassigned = await fetchData("UNASSIGNED", false, false);
        setUnAssignedBookmark(unassigned.bookmark);
        setResults((prev) => [
            ...prev,
            ...unassigned.results?.map((v: any) => ({
                ...v,
                status: "UNASSIGNED",
            })),
        ]);

        const done = await fetchData("DONE", false, false);
        setDoneBookmark(done.bookmark);
        setResults((prev) => [
            ...prev,
            ...done.results?.map((v: any) => ({
                ...v,
                status: "DONE",
            })),
        ]);

        const cancelled = await fetchData("CANCELLED", false, false);
        setCancelledBookmark(cancelled.bookmark);
        setResults((prev) => [
            ...prev,
            ...cancelled.results?.map((v: any) => ({
                ...v,
                status: "CANCELLED",
            })),
        ]);

        setFirstLoad(true);
        setLoading(false);
    };
    const loadMMore = async () => {
        if (type) return loadMore();
        setLoading(true);

        if (assignedBookmark) {
            const tasks = await fetchData("ASSIGNED", true, false);
            if (tasks.status != "done") {
                setAssignedBookmark(tasks.bookmark);
                setResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: "ASSIGNED",
                    })),
                ]);
            }
        }

        if (unAssignedBookmark) {
            const tasks = await fetchData("UNASSIGNED", true, false);
            if (tasks.status != "done") {
                setUnAssignedBookmark(tasks.bookmark);
                setResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: "UNASSIGNED",
                    })),
                ]);
            }
        }

        if (doneBookmark) {
            const tasks = await fetchData("DONE", true, false);
            if (tasks.status != "done") {
                setDoneBookmark(tasks.bookmark);
                setResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: "DONE",
                    })),
                ]);
            }
        }

        if (cancelledBookmark) {
            const tasks = await fetchData("CANCELLED", true, false);
            if (tasks.status != "done") {
                setCancelledBookmark(tasks.bookmark);
                setResults((prev) => [
                    ...prev,
                    ...tasks.results.map((v: any) => ({
                        ...v,
                        status: "CANCELLED",
                    })),
                ]);
            }
        }

        setLoading(false);
    };
    const loadMore = async () => {
        setLoading(true);

        const { results, bookmark, status } = await fetchData(type, true);

        if (status === "done") return;

        if (type === "ASSIGNED") setAssignedBookmark(bookmark);
        if (type === "UNASSIGNED") setUnAssignedBookmark(bookmark);
        if (type === "DONE") setDoneBookmark(bookmark);
        if (type === "CANCELLED") setCancelledBookmark(bookmark);

        setResults((prev) => [
            ...prev,
            ...results.map((v: any) => ({ ...v, status: type })),
        ]);
        setLoading(false);
    };

    // const keyDownHandler = (e:React.KeyboardEvent<HTMLInputElement>) => {
    //     clearTimeout(myTimeout)
    //     if( e.key == 'Enter' ) return getMData()
    //     myTimeout = setTimeout(getMData, keyDownDelay);
    // }

    useEffect(() => {
        if (firstLoad) getMData();
    }, [type]);

    useEffect(() => {
        if (firstLoad) getMData();
    }, [startDt, endDt]);

    useEffect(() => {
        if (!first) return;
        first = false;
        getMData();
    }, []);
    return (
        <section>
            <h2>UserTaskRun Search</h2>
            <Input
                icon="/search.svg"
                placeholder="Search by assigned User ID or User Group"
                type="text"
                value={user_id}
                onKeyDown={keyDownHandler}
                onChange={(e) => setUserId(e.target.value)}
            />
            <div className="between">
                <div className="btns btns-right">
                    <CalendarB
                        changeEarlyDate={setStartDT}
                        earlyDate={startDt}
                        changeLastDate={setEndDT}
                        lastDate={endDt}
                    />
                    <Label>STATUS:</Label>
                    <Button active={type === ""} onClick={() => setType("")}>
                        All
                    </Button>
                    <Button
                        active={type === "ASSIGNED"}
                        onClick={() => setType("ASSIGNED")}
                    >
                        Assigned
                    </Button>
                    <Button
                        active={type === "UNASSIGNED"}
                        onClick={() => setType("UNASSIGNED")}
                    >
                        Unassigned
                    </Button>
                    <Button
                        active={type === "DONE"}
                        onClick={() => setType("DONE")}
                    >
                        Done
                    </Button>
                    <Button
                        active={type === "CANCELLED"}
                        onClick={() => setType("CANCELLED")}
                    >
                        Cancelled
                    </Button>
                </div>
            </div>
            <div
                style={{ minHeight: "568px" }}
                className={`${
                    results.length === 0
                        ? "flex items-center justify-items-center justify-center"
                        : ""
                }`}
            >
                {results.length > 0 ? (
                    <UserTaskRunSearchTable wfspec={id} results={results} />
                ) : (
                    <Loader />
                )}
            </div>
            <div className="end">
                <div className="btns btns-right">
                    {!!type ? (
                        <>
                            <Label>Rows per load:</Label>
                            <PerPage
                                icon="/expand_more.svg"
                                value={limit}
                                onChange={setLimit}
                                values={[10, 20, 30, 60, 100]}
                            />{" "}
                        </>
                    ) : undefined}
                    <LoadMoreButton
                        loading={loading}
                        disabled={
                            !assignedBookmark &&
                            !unAssignedBookmark &&
                            !doneBookmark &&
                            !cancelledBookmark
                        }
                        onClick={loadMMore}
                    >
                        Load More
                    </LoadMoreButton>
                </div>
            </div>
        </section>
    );
};
