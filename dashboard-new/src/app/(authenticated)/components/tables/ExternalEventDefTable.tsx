import { ExternalEventDefId } from 'littlehorse-client/dist/proto/object_id'
import Link from 'next/link'
import { FC } from 'react'

type Props = {
  items: ExternalEventDefId[]
}
export const ExternalEventDefTable: FC<Props> = ({ items }) => {
  if (items.length === 0) {
    return <div className="flex min-h-[400px] items-center justify-center text-center italic">No ExternalEventDefs</div>
  }

  return (
    <div className="py-4">
      {items.map(({ name }) => (
        <div key={name} className="my-2 flex gap-2">
          <Link className="underline hover:no-underline" href={`/externalEventDef/${name}`}>
            {name}
          </Link>
        </div>
      ))}
    </div>
  )
}
