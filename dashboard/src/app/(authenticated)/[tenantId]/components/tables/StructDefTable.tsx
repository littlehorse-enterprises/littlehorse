import { StructDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'
import VersionTag from '../VersionTag'

export const StructDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as StructDefId[]

  return (
    <div className="py-4">
      {allResults.map(({ name, version }: StructDefId) => (
        <SelectionLink key={`${name}.${version}`} href={`/structDef/${name}/${version}`}>
          <p className="group">{name}</p>
          <VersionTag label={`v${version}`} />
        </SelectionLink>
      ))}
    </div>
  )
}
