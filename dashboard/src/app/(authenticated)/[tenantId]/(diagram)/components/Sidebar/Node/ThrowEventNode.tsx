import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { ThrowEventNode as ThrowEventNodeProto } from 'littlehorse-client/proto'
import { LinkIcon } from 'lucide-react'
import { FC } from 'react'
import { VariableAssignment } from '../Components'
import "./node.css"

export const ThrowEventNode: FC<{ node: ThrowEventNodeProto }> = ({ node }) => {
  const { eventDefId, content } = node

  if (!eventDefId) return null

  return (
    <div className="flex max-w-full flex-1 flex-col">
      <small className="node-title">ThrowEvent</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{eventDefId.name}</p>
        <LinkWithTenant href={`/workflowEventDef/${eventDefId.name}`}>
          <LinkIcon className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600" />
        </LinkWithTenant>
      </div>

      {content && (
        <div className="flex flex-col gap-2">
          <small className="node-title">Content</small>

          <VariableAssignment variableAssigment={content} />
        </div>
      )}
    </div>
  )
}
