import LinkWithTenant from '@/app/(authenticated)/[tenantId]/components/LinkWithTenant'
import { RunChildWfNode, VariableAssignment as VariableAssignmentProto } from 'littlehorse-client/proto'
import { LinkIcon } from 'lucide-react'
import { FC } from 'react'
import { LabelContent, VariableAssignment } from '../Components'
import './node.css'

export const ChildWFNode: FC<{ node: RunChildWfNode }> = ({ node }) => {
  const { wfSpec, majorVersion, inputs } = node
  const wfSpecName = wfSpec?.$case === 'wfSpecName' ? wfSpec.value : wfSpec?.$case === 'wfSpecVar' ? 'variable' : '—'

  const wfSpecLink =
    wfSpecName && wfSpecName !== 'variable' && wfSpecName !== '—' ? `/wfSpec/${wfSpecName}/${majorVersion}.0` : null

  return (
    <div className="flex max-w-full flex-1 flex-col gap-2">
      <small className="node-title">WfSpec</small>
      <div className="mb-2 flex items-center">
        <p className="flex-grow truncate text-lg font-medium">{wfSpecName || '—'}</p>
        {wfSpecLink && (
          <LinkWithTenant href={wfSpecLink}>
            <LinkIcon className="ml-1 h-4 w-4 cursor-pointer hover:text-slate-600" />
          </LinkWithTenant>
        )}
      </div>
      <LabelContent label="Major Version" content={`${majorVersion}`} />
      {inputs && Object.keys(inputs).length > 0 && (
        <>
          <small className="node-title">Inputs</small>
          <div className="flex flex-col gap-2">
            {Object.entries(inputs).map(([name, assignment]: [string, VariableAssignmentProto]) => (
              <div key={name} className="flex items-center">
                <span className="flex-1 truncate bg-gray-200 px-2 font-mono">{name}</span>
                <VariableAssignment variableAssigment={assignment} />
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  )
}
