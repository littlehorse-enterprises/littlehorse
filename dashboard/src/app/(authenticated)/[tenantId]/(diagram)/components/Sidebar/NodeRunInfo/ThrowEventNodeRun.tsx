import { ThrowEventNodeRun as ThrowEventNodeRunProto, WorkflowEvent } from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../Components/NodeVariable'
import useSWR from 'swr'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { getWorkflowEvent } from '../../NodeTypes/ThrowEvent/getWorkflowEvent'
import { getVariableValue } from '@/app/utils'
import { VARIABLE_TYPES } from '@/app/constants'
import { Divider } from '../Components/Divider'

export const ThrowEventNodeRun: FC<{ node: ThrowEventNodeRunProto }> = ({ node }) => {
  const { tenantId } = useWhoAmI()

  const throwEventId = node.workflowEventId
  const key = throwEventId ? ['throwEvent', throwEventId, tenantId] : null
  const { data: nodeThrowEvent } = useSWR<WorkflowEvent | undefined>(key, async () => {
    if (!throwEventId) return undefined
    return getWorkflowEvent({ tenantId, ...throwEventId })
  })
  const variable = nodeThrowEvent?.content
  const variableType = variable?.value?.$case
  return (
    <div className="ml-1">
      <NodeVariable label="Node Type" text="Throw event"></NodeVariable>
      <NodeVariable label="workflowEventId" text={node.workflowEventId?.wfRunId?.id}></NodeVariable>
      <NodeVariable label="workflowEventDefId" text={node.workflowEventId?.workflowEventDefId?.name}></NodeVariable>
      <Divider title="Workflow Event Details"></Divider>
      <NodeVariable label="createdAt" text={nodeThrowEvent?.createdAt} type="date"></NodeVariable>
      <NodeVariable label="position" text={`${nodeThrowEvent?.nodeRunId?.position}`}></NodeVariable>
      <NodeVariable label="threadRunNumber" text={`${nodeThrowEvent?.nodeRunId?.threadRunNumber}`}></NodeVariable>
      {variable && (
        <>
          <div className=" mb-1 ml-1 text-sm font-bold">content:</div>
          <div className="ml-1 flex w-full items-center gap-1">
            <p className="rounded bg-gray-100 px-1 py-1 font-mono text-xs text-fuchsia-500">{variable.value?.$case}</p>

            {variableType && <span className="rounded bg-yellow-100 p-1 text-xs">{VARIABLE_TYPES[variableType]}</span>}
            <p> = </p>
            <div className={'px-2  text-center text-xs'}>
              <p className="text-sx">{getVariableValue(variable)}</p>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
