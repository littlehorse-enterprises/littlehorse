import { TaskDefId } from 'littlehorse-client/proto'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const TaskDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No TaskDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name }: TaskDefId) => (
            <SelectionLink key={name} href={`/taskDef/${name}`}>
              <p className="group">{name}</p>
            </SelectionLink>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
