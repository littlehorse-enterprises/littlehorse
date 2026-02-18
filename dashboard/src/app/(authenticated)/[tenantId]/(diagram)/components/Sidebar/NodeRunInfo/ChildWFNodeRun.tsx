import { wfRunIdToPath } from '@/app/utils/wfRun'
import { RunChildWfNodeRun as RunChildWfNodeRunProto } from 'littlehorse-client/proto'
import { FC } from 'react'
import { InputVariables } from '../Components'
import { NodeVariable } from '../Components/NodeVariable'

export const ChildWFNodeRun: FC<{ node: RunChildWfNodeRunProto }> = ({ node }) => {
  const childWfRunLink = node.childWfRunId ? `/wfRun/${wfRunIdToPath(node.childWfRunId)}` : ''
  const wfSpecLink =
    node.wfSpecId && `/wfSpec/${node.wfSpecId.name}/${node.wfSpecId.majorVersion}/${node.wfSpecId.revision}`

  const inputVars =
    node.inputs && Object.keys(node.inputs).length > 0
      ? Object.entries(node.inputs).map(([varName, value]) => ({
          varName,
          value: value!,
          masked: false,
        }))
      : []

  return (
    <div className="ml-1 flex max-w-full flex-1 flex-col">
      <NodeVariable label="Node Type" text="Run Child Workflow" />
      {node.childWfRunId && (
        <NodeVariable label="Child WfRun Id" text={node.childWfRunId.id} type="link" link={childWfRunLink} />
      )}
      {node.wfSpecId && wfSpecLink && (
        <NodeVariable
          label="WfSpec"
          text={`${node.wfSpecId.name} ${node.wfSpecId.majorVersion}.${node.wfSpecId.revision}`}
          type="link"
          link={wfSpecLink}
        />
      )}
      {inputVars.length > 0 && <InputVariables variables={inputVars} />}
    </div>
  )
}
