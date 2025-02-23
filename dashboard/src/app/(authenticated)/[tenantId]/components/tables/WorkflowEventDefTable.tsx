import { WorkflowEventDefId } from 'littlehorse-client/proto'
import { useParams } from 'next/navigation'
import { FC, Fragment } from 'react'
import { SearchResultProps } from '.'
import LinkWithTenant from '../LinkWithTenant'
import { SelectionLink } from '../SelectionLink'

export const WorkflowEventDefTable: FC<SearchResultProps> = ({ pages = [] }) => {
  const { tenantId } = useParams()

  if (pages.every(page => page.results.length === 0)) {
    return <div className="flex min-h-[360px] items-center justify-center text-center italic">No WorkflowEventDefs</div>
  }

  return (
    <div className="py-4">
      {pages.map((page, i) => (
        <Fragment key={i}>
          {page.results.map(({ name }: WorkflowEventDefId) => (
            <SelectionLink key={name} href={`/workflowEventDef/${name}`}>
              <p className="group">{name}</p>
            </SelectionLink>
          ))}
        </Fragment>
      ))}
    </div>
  )
}
