import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { StructDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import LinkWithTenant from '../LinkWithTenant'
import VersionTag from '../VersionTag'
import { TableWrapper } from './TableWrapper'

export const StructDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as StructDefId[]

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No StructDefs</div>
  }

  return (
    <TableWrapper>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Name</TableHead>
            <TableHead>Version</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {allResults.map(({ name, version }: StructDefId) => (
            <TableRow key={`${name}.${version}`} className="hover:bg-gray-50">
              <TableCell>
                <LinkWithTenant
                  href={`/structDef/${name}/${version}`}
                  className="font-medium text-blue-600 hover:text-blue-800 hover:underline"
                >
                  {name}
                </LinkWithTenant>
              </TableCell>
              <TableCell>
                <div className="flex">
                  <VersionTag label={`v${version}`} />
                </div>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableWrapper>
  )
}
