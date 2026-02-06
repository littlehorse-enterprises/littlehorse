import { UserTaskDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'
import VersionTag from '../VersionTag'

export const UserTaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as UserTaskDefId[]

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No UserTaskDefs</div>
  }

  return (
    <div className="py-4">
      {allResults.map(({ name, version }: UserTaskDefId) => (
        <SelectionLink key={`${name}.${version}`} href={`/userTaskDef/${name}/${version}`}>
          <p className="group">{name}</p>
          <VersionTag label={`Latest: v${version}`} />
        </SelectionLink>
      ))}
    </div>
  )
}
