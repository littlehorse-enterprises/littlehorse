import { UserTaskDefId } from 'littlehorse-client/proto'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'
import VersionTag from '../VersionTag'

export const UserTaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No UserTaskDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name, version }: UserTaskDefId) => (
            <SelectionLink key={`${name}.${version}`} href={`/userTaskDef/${name}/${version}`}>
              <p className="group">{name}</p>
              <VersionTag label={`Latest: v${version}`} />
            </SelectionLink>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
