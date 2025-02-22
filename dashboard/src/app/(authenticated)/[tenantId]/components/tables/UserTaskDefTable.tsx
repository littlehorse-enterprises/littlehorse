import { UserTaskDefId } from 'littlehorse-client/proto'
import { TagIcon } from 'lucide-react'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import LinkWithTenant from '../LinkWithTenant'

export const UserTaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No UserTaskDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name, version }: UserTaskDefId) => (
            <Fragment key={`${name}.${version}`}>
              <div className="my-2 flex gap-2">
                <LinkWithTenant className="underline hover:no-underline" href={`/userTaskDef/${name}/${version}`}>
                  {name}
                </LinkWithTenant>
                <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                  <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{version}
                </div>
              </div>
            </Fragment>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
