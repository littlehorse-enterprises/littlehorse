import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { ExternalEventNode as ExternalEventNodeProto } from 'littlehorse-client/proto'
import { LinkIcon } from 'lucide-react'
import { FC } from 'react'
import { VariableAssignment } from '../Components'
import './node.css'

export const ExternalEventNode: FC<{ node: ExternalEventNodeProto }> = ({ node }) => {
  const { externalEventDefId, timeoutSeconds, correlationKey } = node
  if (!externalEventDefId) return null
  return (
    <div className="flex max-w-full flex-1 flex-col">
      <small className="node-title">ExternalEvent</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{externalEventDefId.name}</p>

        <LinkWithTenant href={`/externalEventDef/${externalEventDefId.name}`}>
          <LinkIcon className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600" />
        </LinkWithTenant>
      </div>
      {timeoutSeconds && (
        <div className="flex gap-4">
          <div className="flex flex-1 flex-col gap-2">
            <small className="node-title">Timeout</small>
            <p className="text-lg font-medium">
              <VariableAssignment variableAssigment={timeoutSeconds} />
            </p>
          </div>
        </div>
      )}

      {correlationKey && (
        <div className="flex gap-4">
          <div className="flex flex-1 flex-col gap-2">
            <small className="node-title">Correlation Key</small>
            <p className="text-lg font-medium">
              <VariableAssignment variableAssigment={correlationKey} />
            </p>
          </div>
        </div>
      )}
    </div>
  )
}
