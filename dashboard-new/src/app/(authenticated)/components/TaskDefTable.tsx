import { TaskDefId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  items: TaskDefId[]
}
export const TaskDefTable: FC<Props> = ({ items }) => {
  return (
    <div className="py-4">
      {items.map(({ name }) => (
        <div key={name} className="flex my-2 gap-2">
          <Link className="underline hover:no-underline" href={`/taskDef/${name}`}>
            {name}
          </Link>
        </div>
      ))}
    </div>
  )
}
