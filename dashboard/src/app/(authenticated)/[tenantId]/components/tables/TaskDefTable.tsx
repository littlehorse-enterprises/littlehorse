import { getTaskDefStats, getTaskDefs } from '@/app/actions/getTaskDefs'
import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { routes } from '@/app/routes'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { TaskDefData } from '@/types'
import { TaskDefId } from 'littlehorse-client/proto'
import { Calendar, LayersIcon, RefreshCwIcon, UsersIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useEffect, useMemo, useRef, useState } from 'react'
import { SearchResultProps } from '.'
import LinkWithTenant from '../LinkWithTenant'
import { SortBy, SortOrder } from '../SearchHeader'
import { formatDate, getSortIcon, handleDateSort, handleNameSort, sortData } from './tableUtils'
import { TableWrapper } from './TableWrapper'

type TaskDefTableProps = SearchResultProps & {
  sortBy?: SortBy
  sortOrder?: SortOrder
  setSortBy: (sortBy: SortBy) => void
  setSortOrder: (order: SortOrder) => void
}

const formatCount = (value: number | null) => (value === null ? '—' : value.toLocaleString())

const formatInputs = (count: number) => (count === 0 ? 'None' : count === 1 ? '1 var' : `${count} vars`)

export const TaskDefTable: FC<TaskDefTableProps> = ({
  pages = [],
  sortBy,
  sortOrder = 'asc',
  setSortBy,
  setSortOrder,
}) => {
  const tenantId = useParams().tenantId as string
  const [taskDefs, setTaskDefs] = useState<TaskDefData[]>([])
  const [isLoading, setIsLoading] = useState(false)
  const loadedStatsRef = useRef<Set<string>>(new Set())

  useEffect(() => {
    const taskDefNames = pages.flatMap(page => page.results).map((taskDef: TaskDefId) => taskDef.name)
    if (taskDefNames.length === 0) {
      setTaskDefs([])
      loadedStatsRef.current = new Set()
      return
    }

    setIsLoading(true)
    getTaskDefs(tenantId, taskDefNames)
      .then(rows => {
        setTaskDefs(prev => {
          const statsByName = new Map(prev.map(row => [row.name, row]))
          return rows.map(row => {
            const existing = statsByName.get(row.name)
            if (!existing) return row
            return {
              ...row,
              connectedWorkers: existing.connectedWorkers,
              queueDepth: existing.queueDepth,
            }
          })
        })
      })
      .finally(() => setIsLoading(false))
  }, [pages, tenantId])

  useEffect(() => {
    const taskDefNames = pages.flatMap(page => page.results).map((taskDef: TaskDefId) => taskDef.name)
    const namesNeedingStats = taskDefNames.filter(name => !loadedStatsRef.current.has(name))
    if (namesNeedingStats.length === 0) return

    let cancelled = false
    getTaskDefStats(tenantId, namesNeedingStats).then(statsByName => {
      if (cancelled) return

      namesNeedingStats.forEach(name => loadedStatsRef.current.add(name))
      setTaskDefs(prev =>
        prev.map(row => {
          const stats = statsByName[row.name]
          if (!stats) return row
          return { ...row, ...stats }
        })
      )
    })

    return () => {
      cancelled = true
    }
  }, [pages, tenantId])

  const sortedTaskDefs = useMemo(() => {
    return sortData(taskDefs, sortBy, sortOrder)
  }, [taskDefs, sortBy, sortOrder])

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No TaskDefs</div>
  }

  return (
    <TableWrapper>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>
              <button
                onClick={() => handleNameSort(sortBy, sortOrder, setSortBy, setSortOrder)}
                className="flex items-center transition-colors hover:text-gray-900"
              >
                Name
                {getSortIcon('name', sortBy, sortOrder)}
              </button>
            </TableHead>
            <TableHead>Inputs</TableHead>
            <TableHead>Output</TableHead>
            <TableHead>
              <span className="inline-flex items-center gap-1">
                <UsersIcon className="h-4 w-4" aria-hidden />
                Workers
              </span>
            </TableHead>
            <TableHead>
              <span className="inline-flex items-center gap-1">
                <LayersIcon className="h-4 w-4" aria-hidden />
                Queue
              </span>
            </TableHead>
            <TableHead>
              <button
                onClick={() => handleDateSort(sortBy, sortOrder, setSortBy, setSortOrder)}
                className="flex items-center transition-colors hover:text-gray-900"
              >
                <Calendar className="mr-1 h-4 w-4" />
                Created At
                {getSortIcon('createdAt', sortBy, sortOrder)}
              </button>
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading && sortedTaskDefs.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="h-32 text-center text-muted-foreground">
                <RefreshCwIcon className="mx-auto mb-2 h-6 w-6 animate-spin text-blue-500" />
                Loading TaskDefs…
              </TableCell>
            </TableRow>
          ) : (
            sortedTaskDefs.map(taskDef => (
              <TableRow key={taskDef.name} className="hover:bg-gray-50">
                <TableCell>
                  <LinkWithTenant
                    href={routes.taskDef.detail(taskDef.name)}
                    className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    {taskDef.name}
                  </LinkWithTenant>
                  {taskDef.description ? (
                    <p className="mt-0.5 line-clamp-1 text-sm text-muted-foreground">{taskDef.description}</p>
                  ) : null}
                </TableCell>
                <TableCell className="text-sm text-gray-600">{formatInputs(taskDef.inputVarCount)}</TableCell>
                <TableCell>
                  <TypeDisplay definedType={taskDef.returnType} />
                </TableCell>
                <TableCell className="tabular-nums text-gray-700">{formatCount(taskDef.connectedWorkers)}</TableCell>
                <TableCell className="tabular-nums text-gray-700">{formatCount(taskDef.queueDepth)}</TableCell>
                <TableCell className="text-gray-600">{formatDate(taskDef.createdAt)}</TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </TableWrapper>
  )
}
