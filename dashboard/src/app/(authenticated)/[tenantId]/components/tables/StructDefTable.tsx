import { StructDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const StructDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as StructDefId[]

  return (
    <div className="py-4">
      {allResults.map(({ name, version }: StructDefId) => (
        <SelectionLink key={name} href={`/structDef/${name}/${version}`}>
          <p className="group">{name}</p>
        </SelectionLink>
      ))}
    </div>
  )
}
