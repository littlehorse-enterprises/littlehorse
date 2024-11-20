import { WfSpecId } from 'littlehorse-client/proto'
import { TagIcon } from 'lucide-react'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { useParams } from 'next/navigation'
import LinkWithTenant from '../LinkWithTenant'

export const WfSpecTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name, majorVersion, revision }: WfSpecId) => (
            <div key={`${name}.${majorVersion}.${revision}`} className="my-2 flex gap-2">
              <LinkWithTenant
                className="underline hover:no-underline"
                href={`/wfSpec/${name}/${majorVersion}.${revision}`}
              >
                {name}
              </LinkWithTenant>
              <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
                <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{majorVersion}.{revision}
              </div>
            </div>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
