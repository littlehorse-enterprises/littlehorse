import { ExternalEventDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const ExternalEventDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as ExternalEventDefId[]

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No ExternalEventDefs</div>
  }

  return (
    <div className="py-4">
      {allResults.map(({ name }: ExternalEventDefId) => (
        <SelectionLink key={name} href={`/externalEventDef/${name}`}>
          <p className="group">{name}</p>
        </SelectionLink>
      ))}
    </div>
  )
}
