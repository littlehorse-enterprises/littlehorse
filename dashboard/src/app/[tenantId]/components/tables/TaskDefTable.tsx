import { TaskDefId } from 'littlehorse-client/proto'
import Link from 'next/link'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { useParams } from 'next/navigation'

export const TaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const { tenantId } = useParams()

  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No TaskDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name }: TaskDefId) => (
            <div key={name} className="my-2 flex gap-2">
              <Link className="underline hover:no-underline" href={`/${tenantId}/taskDef/${name}`}>
                {name}
              </Link>
            </div>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
