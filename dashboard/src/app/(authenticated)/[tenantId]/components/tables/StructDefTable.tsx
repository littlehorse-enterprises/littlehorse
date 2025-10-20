import { StructDefId } from 'littlehorse-client/proto'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const StructDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name, version }: StructDefId) => (
            <SelectionLink key={name} href={`/structDef/${name}/${version}`}>
              <p className="group">{name}</p>
            </SelectionLink>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
