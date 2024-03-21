import { TagIcon } from '@heroicons/react/16/solid'
import { WfSpecId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  items: WfSpecId[]
}
export const WfSpecTable: FC<Props> = ({ items }) => {
  if (items.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WfSpecs</div>
  }

  return (
    <div className="py-4">
      {items.map(({ name, majorVersion, revision }) => (
        <div key={`${name}.${majorVersion}.${revision}`} className="my-2 flex gap-2">
          <Link className="underline hover:no-underline" href={`/wfSpec/${name}/${majorVersion}.${revision}`}>
            {name}
          </Link>
          <div className="flex items-center gap-2 rounded bg-blue-200 px-2 font-mono text-sm text-gray-500">
            <TagIcon className="h-4 w-4 fill-none stroke-gray-500 stroke-1" />v{majorVersion}.{revision}
          </div>
        </div>
      ))}
    </div>
  )
}
