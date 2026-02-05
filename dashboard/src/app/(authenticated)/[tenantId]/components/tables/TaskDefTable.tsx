import { getTaskDefs } from '@/app/actions/getTaskDefs'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { TaskDefData } from '@/types'
import { TaskDefId } from 'littlehorse-client/proto'
import { Calendar } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useEffect, useMemo, useState } from 'react'
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

export const TaskDefTable: FC<TaskDefTableProps> = ({
  pages = [],
  sortBy,
  sortOrder = 'asc',
  setSortBy,
  setSortOrder,
}) => {
  const tenantId = useParams().tenantId as string
  const [taskDefs, setTaskDefs] = useState<TaskDefData[]>([])

  useEffect(() => {
    const taskDefNames = pages.flatMap(page => page.results).map((taskDef: TaskDefId) => taskDef.name)
    getTaskDefs(tenantId, taskDefNames).then(setTaskDefs)
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
          {sortedTaskDefs.map(taskDef => (
            <TableRow key={taskDef.name} className="hover:bg-gray-50">
              <TableCell>
                <LinkWithTenant
                  href={`/taskDef/${taskDef.name}`}
                  className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
                >
                  {taskDef.name}
                </LinkWithTenant>
              </TableCell>
              <TableCell className="text-gray-600">{formatDate(taskDef.createdAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableWrapper>
  )
}
