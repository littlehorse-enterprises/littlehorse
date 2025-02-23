import { ExternalEventDefId } from 'littlehorse-client/proto'
import LinkWithTenant from '../LinkWithTenant'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const ExternalEventDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.length === 0) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No ExternalEventDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name }: ExternalEventDefId) => (
            <SelectionLink key={name} href={`/externalEventDef/${name}`}>
              <p className="group">{name}</p>
            </SelectionLink>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
