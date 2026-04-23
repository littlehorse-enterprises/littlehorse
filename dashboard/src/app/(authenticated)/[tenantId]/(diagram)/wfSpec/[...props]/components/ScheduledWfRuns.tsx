'use client'

import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import VersionTag from '@/app/(authenticated)/[tenantId]/components/VersionTag'
import { FUTURE_TIME_RANGES, TimeRange } from '@/app/constants'
import { formatDateReadable, getVariableValue, utcToLocalDateTime, wfRunIdToPath } from '@/app/utils'
import { getCronTimeWindow } from '@/app/utils/getCronTimeWindow'
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { parseExpression } from 'cron-parser'
import { ScheduledWfRun, WfSpec, WfSpecId } from 'littlehorse-client/proto'
import { ClockIcon, RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { useMemo, useState } from 'react'
import useSWR from 'swr'
import { getScheduleWfSpec } from '../actions/getScheduleWfSpec'
import { VariableValuePillRow } from './VariableValuePillRow'

const variablesSummary = (row: ScheduledWfRun): string => {
  const entries = Object.entries(row.variables ?? {})
  if (entries.length === 0) return '—'
  return entries.map(([k, v]) => `${k}: ${getVariableValue(v)}`).join(' · ')
}

const nextRunLabel = (cronExpression: string) =>
  utcToLocalDateTime(parseExpression(cronExpression).next().toDate().toISOString())

const ScheduledWfSpecRef = ({
  wfSpecId,
  isolateRowClick,
}: {
  wfSpecId: WfSpecId | undefined
  isolateRowClick?: boolean
}) => {
  if (!wfSpecId?.name) {
    return <span className="text-muted-foreground">—</span>
  }
  const { name, majorVersion, revision } = wfSpecId
  const versionPath = `${majorVersion}.${revision}`
  const inner = (
    <div className="flex min-w-0 flex-col gap-1.5 sm:flex-row sm:flex-wrap sm:items-center sm:gap-2">
      <LinkWithTenant
        className="truncate font-medium text-blue-600 hover:text-blue-800 hover:underline"
        href={`/wfSpec/${name}/${versionPath}`}
      >
        {name}
      </LinkWithTenant>
      <VersionTag label={`v${versionPath}`} />
    </div>
  )
  if (isolateRowClick) {
    return <div onClick={e => e.stopPropagation()}>{inner}</div>
  }
  return inner
}

export const ScheduledWfRuns = (spec: WfSpec) => {
  const [currentWindow, setWindow] = useState<TimeRange>(-1)
  const [detailRow, setDetailRow] = useState<ScheduledWfRun | null>(null)
  const tenantId = useParams().tenantId as string

  const fetchScheduledWfRuns = async () => {
    return await getScheduleWfSpec({
      name: spec.id!.name,
      version: spec.id!.majorVersion + '.' + spec.id!.revision,
      tenantId: tenantId,
    })
  }

  const { data: scheduledWfRuns, error } = useSWR(['scheduledWfRuns', spec.id, tenantId], fetchScheduledWfRuns)

  const isLoading = !scheduledWfRuns && !error

  const filteredScheduledWfRuns = useMemo(
    () =>
      (scheduledWfRuns || [])
        .filter(scheduledWfRun => {
          if (currentWindow === -1) return true
          const timeWindow = getCronTimeWindow(scheduledWfRun.cronExpression)
          return timeWindow && timeWindow <= currentWindow
        })
        .sort((a, b) => {
          const timeA = parseExpression(a.cronExpression).next().toDate().getTime()
          const timeB = parseExpression(b.cronExpression).next().toDate().getTime()
          return timeA - timeB
        }),
    [currentWindow, scheduledWfRuns]
  )

  if (isLoading) {
    return (
      <div className="flex min-h-[500px] items-center justify-center">
        <RefreshCwIcon className="h-8 w-8 animate-spin text-blue-500" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-[500px] flex-col items-center justify-center text-red-500">
        <p>Error loading scheduled runs</p>
        <p className="text-sm">{error.message}</p>
      </div>
    )
  }

  return (
    <div className="flex min-h-[500px] flex-col gap-4">
      <div className="flex flex-wrap items-center gap-4">
        <Select value={currentWindow.toString()} onValueChange={value => setWindow(parseInt(value) as TimeRange)}>
          <SelectTrigger className="w-[150px] min-w-fit">
            <div className="flex items-center gap-2">
              <ClockIcon className="h-5 w-5 fill-none stroke-black" />
              <SelectValue>{FUTURE_TIME_RANGES.find(time => time.value === currentWindow)?.label}</SelectValue>
            </div>
          </SelectTrigger>
          <SelectContent>
            {FUTURE_TIME_RANGES.map(time => (
              <SelectItem key={time.value} value={time.value.toString()}>
                {time.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {filteredScheduledWfRuns.length === 0 ? (
        <p className="rounded-md border border-dashed p-8 text-center text-sm text-muted-foreground">
          No scheduled workflow runs {currentWindow === -1 ? 'for this WfSpec' : 'match this time window'}.
        </p>
      ) : (
        <div className="rounded-md border">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="min-w-[8rem]">Schedule ID</TableHead>
                <TableHead className="min-w-[6rem]">Cron</TableHead>
                <TableHead className="min-w-[10rem]">Next run</TableHead>
                <TableHead className="min-w-[8rem]">WfSpec</TableHead>
                <TableHead className="min-w-[9rem]">Created</TableHead>
                <TableHead className="min-w-[7rem]">Parent WfRun</TableHead>
                <TableHead>Input variables</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredScheduledWfRuns.map(row => {
                const idStr = row.id?.id ?? '—'
                return (
                  <TableRow
                    key={idStr}
                    role="button"
                    tabIndex={0}
                    className="cursor-pointer"
                    onClick={() => setDetailRow(row)}
                    onKeyDown={e => {
                      if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault()
                        setDetailRow(row)
                      }
                    }}
                    aria-label={`View details for scheduled run ${idStr}`}
                  >
                    <TableCell className="max-w-[14rem] font-mono text-xs">{idStr}</TableCell>
                    <TableCell className="whitespace-nowrap font-mono text-xs">{row.cronExpression || '—'}</TableCell>
                    <TableCell className="whitespace-nowrap text-sm">
                      {row.cronExpression ? nextRunLabel(row.cronExpression) : '—'}
                    </TableCell>
                    <TableCell className="text-sm">
                      <ScheduledWfSpecRef isolateRowClick wfSpecId={row.wfSpecId} />
                    </TableCell>
                    <TableCell className="whitespace-nowrap text-sm">
                      {row.createdAt ? formatDateReadable(row.createdAt) : '—'}
                    </TableCell>
                    <TableCell className="text-sm" onClick={e => e.stopPropagation()}>
                      {row.parentWfRunId?.id ? (
                        <LinkWithTenant
                          className="text-blue-600 hover:underline"
                          href={`/wfRun/${wfRunIdToPath(row.parentWfRunId)}`}
                          linkStyle
                        >
                          {row.parentWfRunId.id}
                        </LinkWithTenant>
                      ) : (
                        '—'
                      )}
                    </TableCell>
                    <TableCell className="max-w-lg align-top">
                      {Object.keys(row.variables ?? {}).length === 0 ? (
                        <span className="text-sm text-muted-foreground">—</span>
                      ) : (
                        <div className="flex min-w-0 flex-col gap-1.5" title={variablesSummary(row)}>
                          {Object.entries(row.variables).map(([name, v]) => (
                            <VariableValuePillRow key={name} varName={name} value={v} />
                          ))}
                        </div>
                      )}
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </div>
      )}

      <Dialog
        open={detailRow != null}
        onOpenChange={open => {
          if (!open) setDetailRow(null)
        }}
      >
        <DialogContent
          className="max-h-[min(90vh,720px)] max-w-2xl overflow-y-auto"
          onOpenAutoFocus={e => e.preventDefault()}
        >
          {detailRow && (
            <>
              <DialogHeader>
                <DialogTitle className="font-mono text-base">Scheduled WfRun: {detailRow.id?.id}</DialogTitle>
              </DialogHeader>
              <dl className="grid grid-cols-[8rem_1fr] gap-x-4 gap-y-3 text-sm">
                <dt className="text-muted-foreground">Cron</dt>
                <dd className="font-mono text-xs">{detailRow.cronExpression || '—'}</dd>
                <dt className="text-muted-foreground">Next run</dt>
                <dd>{detailRow.cronExpression ? nextRunLabel(detailRow.cronExpression) : '—'}</dd>
                <dt className="text-muted-foreground">WfSpec</dt>
                <dd>
                  <ScheduledWfSpecRef wfSpecId={detailRow.wfSpecId} />
                </dd>
                <dt className="text-muted-foreground">Created</dt>
                <dd>{detailRow.createdAt ? formatDateReadable(detailRow.createdAt) : '—'}</dd>
                <dt className="text-muted-foreground">Parent WfRun</dt>
                <dd>
                  {detailRow.parentWfRunId?.id ? (
                    <LinkWithTenant
                      className="text-blue-600 hover:underline"
                      href={`/wfRun/${wfRunIdToPath(detailRow.parentWfRunId)}`}
                    >
                      {detailRow.parentWfRunId.id}
                    </LinkWithTenant>
                  ) : (
                    '—'
                  )}
                </dd>
              </dl>
              <div className="mt-4">
                <h3 className="mb-2 text-sm font-medium">Input variables</h3>
                {Object.keys(detailRow.variables ?? {}).length === 0 ? (
                  <p className="text-sm text-muted-foreground">No input variables configured for this schedule.</p>
                ) : (
                  <div className="ml-0 flex w-full min-w-0 flex-col gap-2 rounded-md border bg-muted/30 p-3">
                    {Object.entries(detailRow.variables).map(([name, v]) => (
                      <VariableValuePillRow key={name} valueBreakWords varName={name} value={v} />
                    ))}
                  </div>
                )}
              </div>
            </>
          )}
        </DialogContent>
      </Dialog>
    </div>
  )
}
