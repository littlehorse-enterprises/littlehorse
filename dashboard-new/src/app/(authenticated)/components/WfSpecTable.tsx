import { TagIcon } from '@heroicons/react/16/solid'
import { WfSpecId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  items: WfSpecId[]
}
export const WfSpecTable: FC<Props> = ({ items }) => {
  return (
    <div className="py-4">
      {items.map(({ name, majorVersion, revision }) => (
        <div key={`${name}.${majorVersion}.${revision}`} className="flex my-2 gap-2">
          <Link className="underline hover:no-underline" href={`/wfSpec/${name}/${majorVersion}.${revision}`}>
            {name}
          </Link>
          <div className="bg-blue-200 items-center text-gray-500 text-sm font-mono rounded px-2 flex gap-2">
            <TagIcon className="w-4 h-4 fill-none stroke-1 stroke-gray-500" />
            v{majorVersion}.{revision}
          </div>
        </div>
      ))}
    </div>
  )
}
