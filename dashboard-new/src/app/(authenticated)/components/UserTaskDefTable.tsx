import { TagIcon } from '@heroicons/react/16/solid'
import { UserTaskDefId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  items: UserTaskDefId[]
}
export const UserTaskDefTable: FC<Props> = ({ items }) => {
  return (
    <div className="py-4">
      {items.map(({ name, version }) => (
        <div key={`${name}.${version}`} className="flex my-2 gap-2">
          <Link className="underline hover:no-underline" href={`/userTaskDef/${name}/${version}`}>
            {name}
          </Link>
          <div className="bg-blue-200 items-center text-gray-500 text-sm font-mono rounded px-2 flex gap-2">
            <TagIcon className="w-4 h-4 fill-none stroke-1 stroke-gray-500" />v{version}
          </div>
        </div>
      ))}
    </div>
  )
}
