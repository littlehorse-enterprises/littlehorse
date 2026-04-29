'use client'

import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { TableWrapper } from '@/app/(authenticated)/[tenantId]/components/tables/TableWrapper'
import VersionTag from '@/app/(authenticated)/[tenantId]/components/VersionTag'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { WfRunResponse } from '@/app/actions/getWfRun'
import { SEARCH_DEFAULT_LIMIT, TIME_RANGES, TimeRange } from '@/app/constants'
import { formatDate, getStatus, getVariableValue, wfRunIdToPath } from '@/app/utils'
import { computeStartTimeWindow, StartTimeWindow } from '@/app/utils/dateTime'
import { getTypedVariableValue, getVariableDefType } from '@/app/utils/variables'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { cn } from '@/lib/utils'
import { LHStatus, Variable, VariableMatch, WfSpec } from 'littlehorse-client/proto'
import { ArrowDown, ArrowUp, ArrowUpDown, RefreshCwIcon } from 'lucide-react'
import { useSearchParams } from 'next/navigation'
import { FC, useCallback, useMemo, useState } from 'react'
import useSWRInfinite, { SWRInfiniteKeyLoader } from 'swr/infinite'
import { PaginatedWfRunResponseList, searchWfRun } from '../actions/searchWfRun'
import { VariableFilter } from './types'
import { VariableValuePillRow } from './VariableValuePillRow'
import { WfRunsHeader } from './WfRunsHeader'
import { WF_RUN_STATUS } from '../../../components/Sidebar/Components/StatusColor'

const EMPTY = '—'

type SortKey = 'startTime' | 'endTime' | 'id'
type SortState = { key: SortKey; dir: 'asc' | 'desc' }

const defaultSort = (): SortState => ({ key: 'startTime', dir: 'desc' })

const buildVariableFilters = (filter: VariableFilter | null): VariableMatch[] => {
  if (!filter?.value?.trim()) return []
  return [
    {
      varName: filter.varDef.name,
      value: getTypedVariableValue(getVariableDefType(filter.varDef), filter.value.trim()),
    },
  ]
}

// Local helper kept distinct from utils/dateTime#formatDuration which takes ms.
const formatRangeDuration = (start?: string, end?: string) => {
  if (!start) return EMPTY
  const s = Date.parse(start)
  if (Number.isNaN(s)) return EMPTY
  const e = end ? Date.parse(end) : Date.now()
  if (Number.isNaN(e)) return EMPTY
  const sec = Math.max(0, Math.floor((e - s) / 1000))
  if (sec < 60) return `${sec}s`
  const m = Math.floor(sec / 60)
  const rs = sec % 60
  if (m < 60) return `${m}m ${rs}s`
  const h = Math.floor(m / 60)
  const rm = m % 60
  return `${h}h ${rm}m`
}

const entrypointVariablesText = (variables: Variable[]) =>
  variables.map(v => `${v.id!.name}: ${getVariableValue(v.value!)}`).join(' · ')

const compareRows = (a: WfRunResponse, b: WfRunResponse, sort: SortState) => {
  const wa = a.wfRun
  const wb = b.wfRun
  const m = sort.dir === 'asc' ? 1 : -1
  const parseT = (t?: string) => {
    if (!t) return 0
    const n = Date.parse(t)
    return Number.isNaN(n) ? 0 : n
  }
  switch (sort.key) {
    case 'startTime': {
      return (parseT(wa.startTime) - parseT(wb.startTime)) * m
    }
    case 'endTime': {
      // Running runs (no endTime) are pinned last regardless of direction.
      const sentinel = sort.dir === 'desc' ? 0 : Number.MAX_SAFE_INTEGER
      const aEnd = wa.endTime ? Date.parse(wa.endTime) : sentinel
      const bEnd = wb.endTime ? Date.parse(wb.endTime) : sentinel
      return ((Number.isNaN(aEnd) ? 0 : aEnd) - (Number.isNaN(bEnd) ? 0 : bEnd)) * m
    }
    case 'id': {
      return (wa.id?.id ?? '').localeCompare(wb.id?.id ?? '') * m
    }
    default:
      return 0
  }
}

const SortableTh: FC<{
  label: string
  sortKey: SortKey
  sort: SortState
  onSort: (k: SortKey) => void
  className?: string
}> = ({ label, sortKey, sort, onSort, className }) => {
  const active = sort.key === sortKey
  return (
    <TableHead className={className}>
      <Button
        type="button"
        variant="ghost"
        className="-ml-1.5 h-8 gap-1 px-1.5 font-medium text-muted-foreground hover:text-foreground"
        onClick={() => onSort(sortKey)}
      >
        {label}
        {active ? (
          sort.dir === 'asc' ? (
            <ArrowUp className="h-3.5 w-3.5 shrink-0" />
          ) : (
            <ArrowDown className="h-3.5 w-3.5 shrink-0" />
          )
        ) : (
          <ArrowUpDown className="h-3.5 w-3.5 shrink-0 opacity-40" />
        )}
      </Button>
    </TableHead>
  )
}

type WfRunsKey = [
  'wfRun',
  LHStatus | 'ALL',
  string,
  number,
  StartTimeWindow,
  string | undefined,
  string,
  number,
  number,
  string,
]

export const WfRuns: FC<WfSpec> = spec => {
  const searchParams = useSearchParams()
  const { tenantId } = useWhoAmI()
  const status = (searchParams.get('status') ? getStatus(searchParams.get('status')) || 'ALL' : 'ALL') as
    | LHStatus
    | 'ALL'
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [window, setWindow] = useState<TimeRange>(TIME_RANGES[0])
  const [sort, setSort] = useState<SortState>(defaultSort)
  const [variableFilter, setVariableFilter] = useState<VariableFilter | null>(null)

  const startTime = useMemo(() => computeStartTimeWindow(window), [window])

  const filterKey = useMemo(
    () => (variableFilter ? `${variableFilter.varDef.name}\0${variableFilter.value}` : ''),
    [variableFilter]
  )

  const onSort = useCallback((key: SortKey) => {
    setSort(prev => {
      if (prev.key === key) {
        return { key, dir: prev.dir === 'asc' ? 'desc' : 'asc' }
      }
      if (key === 'id') {
        return { key, dir: 'asc' }
      }
      return { key, dir: 'desc' }
    })
  }, [])

  const variableFilters = useMemo(() => buildVariableFilters(variableFilter), [variableFilter])

  const wfSpecName = spec.id?.name ?? ''
  const wfSpecMajorVersion = spec.id?.majorVersion ?? 0
  const wfSpecRevision = spec.id?.revision ?? 0

  const getKey: SWRInfiniteKeyLoader<PaginatedWfRunResponseList, WfRunsKey | null> = useCallback(
    (_pageIndex, previousPageData) => {
      if (previousPageData && !previousPageData.bookmarkAsString) return null
      return [
        'wfRun',
        status,
        tenantId,
        limit,
        startTime,
        previousPageData?.bookmarkAsString,
        wfSpecName,
        wfSpecMajorVersion,
        wfSpecRevision,
        filterKey,
      ] as WfRunsKey
    },
    [status, tenantId, limit, startTime, wfSpecName, wfSpecMajorVersion, wfSpecRevision, filterKey]
  )

  const { data, error, size, setSize } = useSWRInfinite<PaginatedWfRunResponseList>(getKey, async (key: WfRunsKey) => {
    const [, wfStatus, tId, lim, stWin, bookmarkAsString, name, major, revision] = key
    return await searchWfRun({
      wfSpecName: name,
      wfSpecMajorVersion: major,
      wfSpecRevision: revision,
      variableFilters,
      limit: lim,
      status: wfStatus === 'ALL' ? undefined : wfStatus,
      tenantId: tId,
      bookmarkAsString,
      ...stWin,
    })
  })

  const rows = useMemo(() => (data ? data.flatMap(p => p.results) : []), [data])
  const sortedRows = useMemo(() => [...rows].sort((a, b) => compareRows(a, b, sort)), [rows, sort])
  const isPending = !data && !error
  const hasNextPage = !!(data && data[data.length - 1]?.bookmarkAsString)

  return (
    <div className="mb-4 flex min-h-0 flex-col">
      <WfRunsHeader
        spec={spec}
        currentStatus={status}
        currentWindow={window}
        setWindow={setWindow}
        variableFilter={variableFilter}
        onVariableFilterChange={f => {
          setVariableFilter(f)
          setSize(1)
        }}
      />

      {isPending ? (
        <div className="flex min-h-[360px] items-center justify-center text-center">
          <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
        </div>
      ) : error ? (
        <p className="min-h-[200px] text-center text-red-500">Failed to load workflow runs: {String(error)}</p>
      ) : (
        <div className="flex min-h-[360px] flex-col">
          {sortedRows.length === 0 ? (
            <p className="rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground">
              No workflow runs for this WfSpec with the current filters{variableFilter ? ' and variable search' : ''}.
            </p>
          ) : (
            <TableWrapper>
              <Table>
                <TableHeader>
                  <TableRow>
                    <SortableTh
                      label="WfRun ID"
                      sortKey="id"
                      sort={sort}
                      onSort={onSort}
                      className="w-[20rem] min-w-[18rem] max-w-[24rem]"
                    />
                    <TableHead className="min-w-[5rem]">Status</TableHead>
                    <SortableTh
                      label="Started"
                      sortKey="startTime"
                      sort={sort}
                      onSort={onSort}
                      className="min-w-[9rem]"
                    />
                    <SortableTh label="Ended" sortKey="endTime" sort={sort} onSort={onSort} className="min-w-[9rem]" />
                    <TableHead className="w-[1%]">Duration</TableHead>
                    <TableHead className="min-w-[6rem]">Parent WfRun</TableHead>
                    <TableHead className="min-w-[12rem]">Entry variables (thread 0)</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sortedRows.map(row => {
                    const w = row.wfRun
                    if (!w.id) return null
                    const path = `/wfRun/${wfRunIdToPath(w.id)}`
                    const entrypointVars = (row.variables ?? []).filter(v => v.id?.threadRunNumber === 0 && v.value)
                    return (
                      <TableRow key={w.id.id} className="hover:bg-muted/50">
                        <TableCell className="w-[20rem] min-w-[18rem] max-w-[24rem] overflow-hidden font-mono text-xs">
                          <LinkWithTenant
                            className="block min-w-0 truncate text-blue-600 hover:underline"
                            href={path}
                            title={w.id.id}
                          >
                            {w.id.id}
                          </LinkWithTenant>
                        </TableCell>
                        <TableCell>
                          <span
                            className={cn(
                              'inline-flex rounded px-2 py-0.5 text-xs font-medium',
                              WF_RUN_STATUS[w.status].backgroundColor,
                              WF_RUN_STATUS[w.status].textColor
                            )}
                          >
                            {w.status}
                          </span>
                        </TableCell>
                        <TableCell className="whitespace-nowrap text-sm text-muted-foreground">
                          {w.startTime ? formatDate(new Date(w.startTime)) : EMPTY}
                        </TableCell>
                        <TableCell className="whitespace-nowrap text-sm text-muted-foreground">
                          {w.endTime ? formatDate(new Date(w.endTime)) : EMPTY}
                        </TableCell>
                        <TableCell className="whitespace-nowrap text-sm text-muted-foreground">
                          {formatRangeDuration(w.startTime, w.endTime)}
                        </TableCell>
                        <TableCell>
                          {w.id.parentWfRunId?.id ? (
                            <LinkWithTenant
                              className="text-sm text-blue-600 hover:underline"
                              href={`/wfRun/${wfRunIdToPath(w.id.parentWfRunId)}`}
                            >
                              {w.id.parentWfRunId.id}
                            </LinkWithTenant>
                          ) : (
                            <span className="text-muted-foreground">{EMPTY}</span>
                          )}
                        </TableCell>
                        <TableCell className="max-w-lg align-top">
                          {entrypointVars.length === 0 ? (
                            <span className="text-sm text-muted-foreground">{EMPTY}</span>
                          ) : (
                            <div
                              className="flex min-w-0 flex-col gap-1.5"
                              title={entrypointVariablesText(entrypointVars)}
                            >
                              {entrypointVars.map(v => (
                                <VariableValuePillRow key={v.id?.name} varName={v.id!.name!} value={v.value!} />
                              ))}
                            </div>
                          )}
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </TableWrapper>
          )}
        </div>
      )}
      <SearchFooter
        currentLimit={limit}
        setLimit={setLimit}
        hasNextPage={hasNextPage}
        fetchNextPage={() => setSize(size + 1)}
      />
      <p className="mt-2 text-xs text-muted-foreground">
        Sorting applies to loaded pages only. Use Load more to add runs to the list and the sort.
      </p>
    </div>
  )
}
