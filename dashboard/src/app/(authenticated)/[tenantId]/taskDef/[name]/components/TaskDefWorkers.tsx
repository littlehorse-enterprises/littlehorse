'use client'

import { getTaskWorkerGroup } from '@/app/(authenticated)/[tenantId]/taskDef/[name]/actions/getTaskWorkerGroup'
import { SEARCH_DEFAULT_LIMIT, SEARCH_LIMITS, SearchLimit } from '@/app/constants'
import { utcToLocalDateTime } from '@/app/utils'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import {
  Pagination,
  PaginationContent,
  PaginationEllipsis,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { getVisiblePageNumbers, paginateSlice } from '@/lib/pagination'
import { useQuery } from '@tanstack/react-query'
import { LHHostInfo, TaskWorkerMetadata } from 'littlehorse-client/proto'
import { ChevronDown, ChevronsLeft, ChevronsRight, RefreshCwIcon, SearchIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useEffect, useMemo, useState } from 'react'

type Props = {
  taskDefName: string
  isRefreshing: boolean
  onRefresh: () => void
}

const formatHosts = (hosts: LHHostInfo[]) =>
  hosts.length === 0 ? '—' : hosts.map(h => `${h.host}:${h.port}`).join(', ')

type WorkerRow = {
  clientId: string
  metadata: TaskWorkerMetadata
}

const sortWorkers = (workers: WorkerRow[]): WorkerRow[] =>
  [...workers].sort((a, b) => {
    const aTime = a.metadata.latestHeartbeat ? Date.parse(a.metadata.latestHeartbeat) : 0
    const bTime = b.metadata.latestHeartbeat ? Date.parse(b.metadata.latestHeartbeat) : 0
    return bTime - aTime
  })

const matchesSearch = (row: WorkerRow, query: string): boolean => {
  const q = query.trim().toLowerCase()
  if (!q) return true
  const { clientId, metadata } = row
  if (clientId.toLowerCase().includes(q)) return true
  if (metadata.taskWorkerId?.toLowerCase().includes(q)) return true
  if (formatHosts(metadata.hosts).toLowerCase().includes(q)) return true
  return false
}

export const TaskDefWorkers: FC<Props> = ({ taskDefName, isRefreshing, onRefresh }) => {
  const tenantId = useParams().tenantId as string
  const [currentPage, setCurrentPage] = useState(1)
  const [pageSize, setPageSize] = useState<SearchLimit>(SEARCH_DEFAULT_LIMIT)
  const [searchQuery, setSearchQuery] = useState('')

  const { data, isPending } = useQuery({
    queryKey: ['taskWorkerGroup', tenantId, taskDefName],
    queryFn: () => getTaskWorkerGroup({ tenantId, taskDefName }),
    refetchInterval: 30_000,
  })

  const allWorkers: WorkerRow[] = useMemo(
    () =>
      data ? sortWorkers(Object.entries(data.taskWorkers).map(([clientId, metadata]) => ({ clientId, metadata }))) : [],
    [data]
  )

  const filteredWorkers = useMemo(
    () => allWorkers.filter(row => matchesSearch(row, searchQuery)),
    [allWorkers, searchQuery]
  )

  const totalPages = Math.max(1, Math.ceil(filteredWorkers.length / pageSize))

  useEffect(() => {
    setCurrentPage(1)
    setSearchQuery('')
  }, [taskDefName])

  useEffect(() => {
    setCurrentPage(1)
  }, [searchQuery, pageSize])

  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages)
  }, [currentPage, totalPages])

  const paginatedWorkers = paginateSlice(filteredWorkers, currentPage, pageSize)
  const rangeStart = filteredWorkers.length === 0 ? 0 : (currentPage - 1) * pageSize + 1
  const rangeEnd = Math.min(currentPage * pageSize, filteredWorkers.length)
  const pageNumbers = getVisiblePageNumbers(currentPage, totalPages)
  const showPagination = filteredWorkers.length > 0

  const goToPage = (page: number) => {
    if (page < 1 || page > totalPages) return
    setCurrentPage(page)
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-start justify-between space-y-0">
        <div>
          <CardTitle className="text-lg">Task workers</CardTitle>
          <CardDescription>
            Workers connected to this TaskDef. Each row is a client session identified by client ID.
          </CardDescription>
        </div>
        <button
          type="button"
          onClick={onRefresh}
          disabled={isRefreshing}
          className="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-60"
          aria-label="Refresh task workers"
        >
          <RefreshCwIcon className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
        </button>
      </CardHeader>
      <CardContent className="space-y-4">
        {allWorkers.length > 0 && (
          <div className="relative max-w-md">
            <SearchIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              type="search"
              placeholder="Filter by client ID, worker ID, or host…"
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="pl-9"
              aria-label="Filter task workers"
            />
          </div>
        )}

        {isPending || isRefreshing ? (
          <div className="flex min-h-[120px] items-center justify-center">
            <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
          </div>
        ) : allWorkers.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">
            No task workers are connected. Start a worker that polls this TaskDef to see it here.
          </p>
        ) : filteredWorkers.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">No workers match your filter.</p>
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead scope="col">Client ID</TableHead>
                  <TableHead scope="col">Worker ID</TableHead>
                  <TableHead scope="col">Last heartbeat</TableHead>
                  <TableHead scope="col">Connected Servers</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {paginatedWorkers.map(({ clientId, metadata }) => (
                  <TableRow key={clientId}>
                    <TableCell className="font-mono text-sm">{clientId}</TableCell>
                    <TableCell className="font-mono text-sm">{metadata.taskWorkerId || '—'}</TableCell>
                    <TableCell>
                      {metadata.latestHeartbeat ? utcToLocalDateTime(metadata.latestHeartbeat) : '—'}
                    </TableCell>
                    <TableCell className="font-mono text-sm">{formatHosts(metadata.hosts)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {showPagination && (
              <div className="flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
                <p className="text-sm text-muted-foreground">
                  Showing {rangeStart.toLocaleString()}–{rangeEnd.toLocaleString()} of{' '}
                  {filteredWorkers.length.toLocaleString()}
                  {searchQuery.trim() && filteredWorkers.length !== allWorkers.length
                    ? ` (${allWorkers.length.toLocaleString()} total)`
                    : ''}
                </p>

                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-6">
                  <div className="flex items-center gap-2">
                    <span className="whitespace-nowrap text-sm text-muted-foreground">Rows per page</span>
                    <div className="relative">
                      <select
                        value={pageSize}
                        onChange={e => setPageSize(parseInt(e.target.value, 10) as SearchLimit)}
                        className="cursor-pointer appearance-none rounded-lg border border-input bg-background px-3 py-1.5 pr-8 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
                        aria-label="Rows per page"
                      >
                        {SEARCH_LIMITS.map(limit => (
                          <option key={limit} value={limit}>
                            {limit}
                          </option>
                        ))}
                      </select>
                      <ChevronDown className="pointer-events-none absolute right-2 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    </div>
                  </div>

                  {totalPages > 1 && (
                    <Pagination className="mx-0 w-auto justify-start">
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationLink
                            href="#"
                            size="icon"
                            aria-label="First page"
                            className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                            onClick={e => {
                              e.preventDefault()
                              goToPage(1)
                            }}
                          >
                            <ChevronsLeft className="h-4 w-4" />
                          </PaginationLink>
                        </PaginationItem>
                        <PaginationItem>
                          <PaginationPrevious
                            href="#"
                            className={currentPage === 1 ? 'pointer-events-none opacity-50' : ''}
                            onClick={e => {
                              e.preventDefault()
                              goToPage(currentPage - 1)
                            }}
                          />
                        </PaginationItem>

                        {pageNumbers.map((item, index) =>
                          item === 'ellipsis' ? (
                            <PaginationItem key={`ellipsis-${index}`}>
                              <PaginationEllipsis />
                            </PaginationItem>
                          ) : (
                            <PaginationItem key={item}>
                              <PaginationLink
                                href="#"
                                isActive={currentPage === item}
                                onClick={e => {
                                  e.preventDefault()
                                  goToPage(item)
                                }}
                              >
                                {item}
                              </PaginationLink>
                            </PaginationItem>
                          )
                        )}

                        <PaginationItem>
                          <PaginationNext
                            href="#"
                            className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                            onClick={e => {
                              e.preventDefault()
                              goToPage(currentPage + 1)
                            }}
                          />
                        </PaginationItem>
                        <PaginationItem>
                          <PaginationLink
                            href="#"
                            size="icon"
                            aria-label="Last page"
                            className={currentPage === totalPages ? 'pointer-events-none opacity-50' : ''}
                            onClick={e => {
                              e.preventDefault()
                              goToPage(totalPages)
                            }}
                          >
                            <ChevronsRight className="h-4 w-4" />
                          </PaginationLink>
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}
                </div>
              </div>
            )}
          </>
        )}
      </CardContent>
    </Card>
  )
}
