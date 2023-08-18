"use client";
import {
  Button,
  Calendar,
  CalendarB,
  Label,
  LoadMoreButton,
  Loader,
  PerPage,
} from "ui";
import { useEffect, useState } from "react";
import { TaskRunSearchTable } from "../components/search/TaskRunSearchTable";
import moment from "moment";

export interface Result {
  wfRunId: string;
  status?: string;
}

const allLimit = 5;
const defaultLimit = 15;
const keyDownDelay = 1000; // miliseconds

let myTimeout: NodeJS.Timeout;

export const TaskRunSearch = ({ id }: any) => {
  let first = true;

  const [loading, setLoading] = useState(false);
  const [firstLoad, setFirstLoad] = useState(false);
  const [limit, setLimit] = useState(defaultLimit);

  const [startDt, setStartDT] = useState<Date>(
    moment().startOf("day").toDate()
  );
  const [endDt, setEndDT] = useState<Date>(moment().toDate());

  const [errorBookmark, setErrorBookmark] = useState();
  const [completedBookmark, setCompletedBookmark] = useState();
  const [startingBookmark, setStartingBookmark] = useState();
  const [runningBookmark, setRunningBookmark] = useState();

  const [type, setType] = useState("");
  const [results, setResults] = useState<any[]>([]);

  const fetchData = async (type: string, paginate = false, useLimit = true) => {
    let bookmark: string | undefined;
    if (type === "TASK_FAILED") bookmark = errorBookmark;
    if (type === "TASK_SUCCESS") bookmark = completedBookmark;
    if (type === "TASK_SCHEDULED") bookmark = startingBookmark;
    if (type === "TASK_RUNNING") bookmark = runningBookmark;
    //TASK_SCHEDULED

    // - TASK_SCHEDULED
    // - TASK_RUNNING
    // - TASK_SUCCESS
    // - TASK_FAILED
    // - TASK_TIMEOUT
    // - TASK_OUTPUT_SERIALIZING_ERROR
    // - TASK_INPUT_VAR_SUB_ERROR

    const filters: any = {
      limit: useLimit ? limit : allLimit,
    };
    // if(prefix?.trim()) filters['prefix'] = prefix.trim().toLocaleLowerCase()
    if (paginate && bookmark) filters["bookmark"] = bookmark;
    if (paginate && !bookmark) return { status: "done" };
    const res = await fetch("/api/search/taskRun", {
      method: "POST",
      body: JSON.stringify({
        statusAndTaskDef: {
          status: type,
          taskDefName: id,
          earliestStart: startDt,
          latestStart: endDt,
        },
        ...filters,
      }),
    });
    if (res.ok) {
      const response = await res.json();
      return { ...response, status: "ok" };
    }
  };
  const getData = async () => {
    setLoading(true);
    const { results, bookmark } = await fetchData(type);
    if (type === "TASK_FAILED") setErrorBookmark(bookmark);
    if (type === "TASK_SUCCESS") setCompletedBookmark(bookmark);
    if (type === "TASK_SCHEDULED") setStartingBookmark(bookmark);
    if (type === "TASK_RUNNING") setRunningBookmark(bookmark);
    setResults(results.map((v: Result) => ({ ...v, status: type })));
    setLoading(false);
  };
  const getMData = async () => {
    setErrorBookmark(undefined);
    setCompletedBookmark(undefined);
    setStartingBookmark(undefined);
    setRunningBookmark(undefined);
    if (type) return getData();

    setLoading(true);
    setResults([]);

    const starting = await fetchData("TASK_SCHEDULED", false, false);
    setStartingBookmark(starting.bookmark);
    setResults((prev) => [
      ...prev,
      ...starting.results?.map((v: any) => ({ ...v, status: "TASK_SCHEDULED" })),
    ]);

    const running = await fetchData("TASK_RUNNING", false, false);
    setRunningBookmark(running.bookmark);
    setResults((prev) => [
      ...prev,
      ...running.results.map((v: any) => ({ ...v, status: "TASK_RUNNING" })),
    ]);

    const completed = await fetchData("TASK_SUCCESS", false, false);
    setCompletedBookmark(completed.bookmark);
    setResults((prev) => [
      ...prev,
      ...completed.results.map((v: any) => ({ ...v, status: "TASK_SUCCESS" })),
    ]);

    const errors = await fetchData("TASK_FAILED", false, false);
    setErrorBookmark(errors.bookmark);
    setResults((prev) => [
      ...prev,
      ...errors.results.map((v: any) => ({ ...v, status: "TASK_FAILED" })),
    ]);

    setFirstLoad(true);
    setLoading(false);
  };
  const loadMMore = async () => {
    if (type) return loadMore();
    setLoading(true);

    if (startingBookmark) {
      const starting = await fetchData("TASK_SCHEDULED", true, false);
      if (starting.status != "done") {
        setStartingBookmark(starting.bookmark);
        setResults((prev) => [
          ...prev,
          ...starting.results.map((v: any) => ({ ...v, status: "TASK_SCHEDULED" })),
        ]);
      }
    }
    if (runningBookmark) {
      const running = await fetchData("TASK_RUNNING", true, false);
      if (running.status != "done") {
        setRunningBookmark(running.bookmark);
        setResults((prev) => [
          ...prev,
          ...running.results.map((v: any) => ({ ...v, status: "TASK_RUNNING" })),
        ]);
      }
    }
    if (completedBookmark) {
      const completed = await fetchData("TASK_SUCCESS", true, false);
      if (completed.status != "done") {
        setCompletedBookmark(completed.bookmark);
        setResults((prev) => [
          ...prev,
          ...completed.results.map((v: any) => ({ ...v, status: "TASK_SUCCESS" })),
        ]);
      }
    }
    if (errorBookmark) {
      const tasks = await fetchData("TASK_FAILED", true, false);
      if (tasks.status != "done") {
        setErrorBookmark(tasks.bookmark);
        setResults((prev) => [
          ...prev,
          ...tasks.results.map((v: any) => ({ ...v, status: "TASK_FAILED" })),
        ]);
      }
    }

    setLoading(false);
  };
  const loadMore = async () => {
    setLoading(true);

    const { results, bookmark, status } = await fetchData(type, true);

    if (status === "done") return;

    if (type === "TASK_FAILED") setErrorBookmark(bookmark);
    if (type === "TASK_SUCCESS") setCompletedBookmark(bookmark);
    if (type === "TASK_SCHEDULED") setStartingBookmark(bookmark);
    if (type === "TASK_RUNNING") setRunningBookmark(bookmark);
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
      <div className="between">
        <h2>TaskRun search</h2>
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
            active={type === "TASK_SCHEDULED"}
            onClick={() => setType("TASK_SCHEDULED")}
          >
            Starting
          </Button>
          <Button
            active={type === "TASK_RUNNING"}
            onClick={() => setType("TASK_RUNNING")}
          >
            Running
          </Button>
          <Button
            active={type === "TASK_SUCCESS"}
            onClick={() => setType("TASK_SUCCESS")}
          >
            Completed
          </Button>
          <Button active={type === "TASK_FAILED"} onClick={() => setType("TASK_FAILED")}>
            Error
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
          <TaskRunSearchTable wfspec={id} results={results} />
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
              !startingBookmark &&
              !runningBookmark &&
              !errorBookmark &&
              !completedBookmark
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
