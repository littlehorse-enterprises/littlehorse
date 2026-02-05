import { WorkflowEventDefId } from 'littlehorse-client/proto'
import { FC } from 'react'
import { SearchResultProps } from '.'
import { SelectionLink } from '../SelectionLink'

export const WorkflowEventDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const allResults = pages.flatMap(page => page.results) as WorkflowEventDefId[]

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WorkflowEventDefs</div>
  }

  return (
    <div className="py-4">
      {allResults.map(({ name }: WorkflowEventDefId) => (
        <SelectionLink key={name} href={`/workflowEventDef/${name}`}>
          <p className="group">{name}</p>
        </SelectionLink>
      ))}
    </div>
  )
}
