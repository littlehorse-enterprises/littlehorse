import { getLatestWfSpecs } from '@/app/actions/getLatestWfSpec'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { WfSpecData } from '@/types'
import { Calendar } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, useEffect, useMemo, useState } from 'react'
import { SearchResultProps } from '.'
import LinkWithTenant from '../LinkWithTenant'
import { SortBy, SortOrder } from '../SearchHeader'
import VersionTag from '../VersionTag'
import { formatDate, getSortIcon, handleDateSort, handleNameSort, sortData } from './tableUtils'
import { TableWrapper } from './TableWrapper'

type WfSpecTableProps = SearchResultProps & {
  sortBy?: SortBy
  sortOrder?: SortOrder
  setSortBy: (sortBy: SortBy) => void
  setSortOrder: (order: SortOrder) => void
}

export const WfSpecTable: FC<WfSpecTableProps> = ({
  pages = [],
  sortBy,
  sortOrder = 'asc',
  setSortBy,
  setSortOrder,
}) => {
  const tenantId = useParams().tenantId as string
  const [wfSpecs, setWfSpecs] = useState<WfSpecData[]>([])

  useEffect(() => {
    const wfSpecNames = pages.flatMap(page => page.results).map(wfSpec => wfSpec.name)
    getLatestWfSpecs(tenantId, wfSpecNames).then(setWfSpecs)
  }, [pages, tenantId])

  const sortedWfSpecs = useMemo(() => {
    return sortData(wfSpecs, sortBy, sortOrder)
  }, [wfSpecs, sortBy, sortOrder])

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
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
            <TableHead>Version</TableHead>
            <TableHead>Parent WfSpec</TableHead>
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
          {sortedWfSpecs.map(wfSpec => (
            <TableRow key={wfSpec.name} className="hover:bg-gray-50">
              <TableCell>
                <LinkWithTenant
                  href={`/wfSpec/${wfSpec.name}/${wfSpec.latestVersion}`}
                  className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
                >
                  {wfSpec.name}
                </LinkWithTenant>
              </TableCell>
              <TableCell>
                <div className="flex">
                  <VersionTag label={`v${wfSpec.latestVersion}`} />
                </div>
              </TableCell>
              <TableCell>
                {wfSpec.parentWfSpec ? (
                  <LinkWithTenant
                    href={`/wfSpec/${wfSpec.parentWfSpec.wfSpecName}/${wfSpec.parentWfSpec.wfSpecMajorVersion}.0`}
                    className="text-sm text-blue-600 hover:text-blue-800 hover:underline"
                  >
                    {wfSpec.parentWfSpec.wfSpecName} (v{wfSpec.parentWfSpec.wfSpecMajorVersion})
                  </LinkWithTenant>
                ) : (
                  <span className="text-sm text-gray-400">—</span>
                )}
              </TableCell>
              <TableCell className="text-gray-600">{formatDate(wfSpec.createdAt)}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableWrapper>
  )
}
