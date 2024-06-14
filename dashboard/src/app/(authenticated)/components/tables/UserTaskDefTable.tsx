import { UserTaskDefId } from 'littlehorse-client'
import { TagIcon } from 'lucide-react'
import Link from 'next/link'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'

export const UserTaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No UserTaskDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name, version }: UserTaskDefId) => (
            <div key={`${name}.${version}`} className="my-2 flex gap-2">
              <Link className="underline hover:no-underline" href={`/userTaskDef/${name}/${version}`}>
                {name}
              </Link>
              <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{version}
              </div>
            </div>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
