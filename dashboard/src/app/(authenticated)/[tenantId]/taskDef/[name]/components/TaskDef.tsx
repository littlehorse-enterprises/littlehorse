'use client'
import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { SearchFooter } from '@/app/(authenticated)/[tenantId]/components/SearchFooter'
import { SelectionLink } from '@/app/(authenticated)/[tenantId]/components/SelectionLink'
import { PaginatedWfSpecList, searchWfSpecs } from '@/app/actions/getWfSpecsByTaskDef'
import { SEARCH_DEFAULT_LIMIT } from '@/app/constants'
import { routes } from '@/app/routes'
import { localDateTimeToUTCIsoString, utcToLocalDateTime, wfRunIdToPath } from '@/app/utils'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Separator } from '@/components/ui/separator'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useInfiniteQuery, useQueryClient } from '@tanstack/react-query'
import { TaskDef as TaskDefProto, TaskStatus } from 'littlehorse-client/proto'
import { RefreshCwIcon, TagIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, Fragment, useCallback, useState } from 'react'
import { mutate } from 'swr'
import { PaginatedTaskRunList, searchTaskRun } from '../actions/searchTaskRun'
import { TaskDefHeader } from './TaskDefHeader'
import { TaskDefMetrics } from './metrics'
import { TaskDefConnectedWorkersCard, TaskDefQueueDepthCard } from './TaskDefOverview'
import { TaskDefWorkers } from './TaskDefWorkers'

type Props = {
  spec: TaskDefProto
}

export const TaskDef: FC<Props> = ({ spec }) => {
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | 'ALL'>('ALL')
  const [createdAfter, setCreatedAfter] = useState('')
  const [createdBefore, setCreatedBefore] = useState('')
  const tenantId = useParams().tenantId as string
  const [limit, setLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [wfSpecLimit, setWfSpecLimit] = useState<number>(SEARCH_DEFAULT_LIMIT)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const queryClient = useQueryClient()
  const taskDefName = spec.id?.name || ''

  const {
    data: wfSpecsData,
    hasNextPage: wfSpecsHasNextPage,
    fetchNextPage: wfSpecsFetchNextPage,
  } = useInfiniteQuery({
    queryKey: ['wfSpecs', tenantId, limit, taskDefName],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedWfSpecList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchWfSpecs({
        tenantId,
        bookmarkAsString: pageParam,
        limit: wfSpecLimit,
        wfSpecCriteria: { $case: 'taskDefName', value: taskDefName },
      })
    },
  })

  const { isPending, data, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ['taskRun', selectedStatus, tenantId, limit, createdAfter, createdBefore, taskDefName],
    initialPageParam: undefined,
    getNextPageParam: (lastPage: PaginatedTaskRunList) => lastPage.bookmarkAsString,
    queryFn: async ({ pageParam }) => {
      return await searchTaskRun({
        tenantId,
        bookmarkAsString: pageParam,
        limit,
        status: selectedStatus == 'ALL' ? undefined : selectedStatus,
        taskDefName,
        earliestStart: createdAfter ? localDateTimeToUTCIsoString(createdAfter) : undefined,
        latestStart: createdBefore ? localDateTimeToUTCIsoString(createdBefore) : undefined,
      })
    },
  })

  const refreshAll = useCallback(async () => {
    setIsRefreshing(true)
    try {
      await Promise.all([
        queryClient.refetchQueries({ queryKey: ['taskDefQueueDepth', tenantId, taskDefName] }),
        queryClient.refetchQueries({ queryKey: ['taskWorkerGroup', tenantId, taskDefName] }),
        queryClient.refetchQueries({
          queryKey: ['taskRun', selectedStatus, tenantId, limit, createdAfter, createdBefore, taskDefName],
        }),
        queryClient.refetchQueries({ queryKey: ['wfSpecs', tenantId, limit, taskDefName] }),
        mutate(key => Array.isArray(key) && key[0] === 'taskMetrics' && key[1] === taskDefName),
      ])
    } finally {
      setIsRefreshing(false)
    }
  }, [
    queryClient,
    tenantId,
    taskDefName,
    selectedStatus,
    limit,
    createdAfter,
    createdBefore,
  ])

  return (
    <div className="space-y-8 pb-12">
      <Navigation href={routes.search.homeWithType('TaskDef')} title="Go back to TaskDefs" />

      <div className="flex flex-col gap-4">
        <div className="flex justify-end">
          <button
            type="button"
            onClick={refreshAll}
            disabled={isRefreshing}
            className="inline-flex items-center gap-1.5 rounded-md px-2 py-1 text-sm text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:pointer-events-none disabled:opacity-60"
            aria-label="Refresh overview"
          >
            <RefreshCwIcon className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
        <div className="grid grid-cols-1 gap-4 md:grid-cols-[1.2fr_1fr_1fr]">
          <TaskDefHeader spec={spec} className="h-full" />
          <TaskDefQueueDepthCard taskDefName={taskDefName} isRefreshing={isRefreshing} />
          <TaskDefConnectedWorkersCard taskDefName={taskDefName} isRefreshing={isRefreshing} />
        </div>
      </div>

      <TaskDefWorkers taskDefName={taskDefName} isRefreshing={isRefreshing} onRefresh={refreshAll} />

      {spec.id && <TaskDefMetrics taskDefId={spec.id} />}

      <div className="grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-lg">WfSpec usage</CardTitle>
            <CardDescription>WfSpecs that reference this TaskDef</CardDescription>
          </CardHeader>
          <CardContent>
            {wfSpecsData ? (
              <div className="relative flex max-h-[320px] flex-col overflow-auto">
                {isRefreshing ? (
                  <div className="absolute inset-0 z-10 flex items-center justify-center rounded-md bg-background/60">
                    <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
                  </div>
                ) : null}
                {wfSpecsData.pages
                  .flatMap(page => page.results)
                  .map(wfSpec => (
                    <Fragment key={`${wfSpec.name}-${wfSpec.majorVersion}-${wfSpec.revision}`}>
                      <SelectionLink
                        href={routes.wfSpec.detail(wfSpec.name, `${wfSpec.majorVersion}.${wfSpec.revision}`)}
                      >
                        <p className="group">{wfSpec.name}</p>
                        <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                          <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{wfSpec.majorVersion}.
                          {wfSpec.revision}
                        </div>
                      </SelectionLink>
                      <Separator />
                    </Fragment>
                  ))}
                <SearchFooter
                  currentLimit={wfSpecLimit}
                  setLimit={setWfSpecLimit}
                  hasNextPage={wfSpecsHasNextPage}
                  fetchNextPage={wfSpecsFetchNextPage}
                />
              </div>
            ) : (
              <div className="flex min-h-[120px] items-center justify-center">
                <RefreshCwIcon className="h-6 w-6 animate-spin text-blue-500" />
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="lg:col-span-3">
          <CardHeader>
            <CardTitle className="text-lg">Related TaskRuns</CardTitle>
            <CardDescription>Search TaskRuns for this TaskDef</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex flex-col gap-4 xl:flex-row xl:items-end xl:gap-5">
              <div className="flex min-w-0 flex-1 flex-col gap-1">
                <Label htmlFor="task-run-status">Status</Label>
                <select
                  id="task-run-status"
                  className="rounded border px-2 py-2"
                  value={selectedStatus}
                  onChange={(e: React.ChangeEvent<HTMLSelectElement>) => {
                    setSelectedStatus(e.target.value as TaskStatus | 'ALL')
                  }}
                >
                  <option value="ALL">ALL</option>
                  {Object.keys(TaskStatus)
                    .filter(status => status != TaskStatus.UNRECOGNIZED)
                    .map(status => (
                      <option key={status} value={status}>
                        {status}
                      </option>
                    ))}
                </select>
              </div>
              <div className="flex min-w-0 flex-1 flex-col gap-1">
                <Label htmlFor="created-after">Created after</Label>
                <Input
                  id="created-after"
                  type="datetime-local"
                  value={createdAfter}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedAfter(e.target.value)}
                />
              </div>
              <div className="flex min-w-0 flex-1 flex-col gap-1">
                <Label htmlFor="created-before">Created before</Label>
                <Input
                  id="created-before"
                  type="datetime-local"
                  value={createdBefore}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setCreatedBefore(e.target.value)}
                />
              </div>
            </div>

            {isPending || (isRefreshing && !data) ? (
              <div className="flex min-h-[280px] items-center justify-center">
                <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
              </div>
            ) : (
              <div className="relative">
                {isRefreshing ? (
                  <div className="absolute inset-0 z-10 flex min-h-[280px] items-center justify-center rounded-md bg-background/60">
                    <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
                  </div>
                ) : null}
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead scope="col">WfRun Id</TableHead>
                    <TableHead scope="col">Task GUID</TableHead>
                    <TableHead scope="col">Creation Date</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {data?.pages.some(page => page.resultsWithDetails.length > 0) ? (
                    data.pages.map((page, i) => (
                      <Fragment key={i}>
                        {page.resultsWithDetails.map(({ taskRun }) => {
                          if (!taskRun.id?.wfRunId) return null
                          return (
                            <TableRow key={taskRun.id?.taskGuid}>
                              <TableCell>
                                <LinkWithTenant
                                  className="text-blue-500 hover:underline"
                                  target="_blank"
                                  href={`${routes.wfRun.detail(wfRunIdToPath(taskRun.id.wfRunId))}?threadRunNumber=${taskRun.source?.taskRunSource?.value.nodeRunId?.threadRunNumber}`}
                                >
                                  {wfRunIdToPath(taskRun.id.wfRunId)}
                                </LinkWithTenant>
                              </TableCell>
                              <TableCell className="font-mono text-sm">{taskRun.id?.taskGuid}</TableCell>
                              <TableCell>
                                {taskRun.scheduledAt ? utcToLocalDateTime(taskRun.scheduledAt) : 'N/A'}
                              </TableCell>
                            </TableRow>
                          )
                        })}
                      </Fragment>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={3} className="text-center text-muted-foreground">
                        No TaskRuns match these filters
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
              </div>
            )}

            <SearchFooter
              currentLimit={limit}
              setLimit={setLimit}
              hasNextPage={hasNextPage}
              fetchNextPage={fetchNextPage}
            />
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
