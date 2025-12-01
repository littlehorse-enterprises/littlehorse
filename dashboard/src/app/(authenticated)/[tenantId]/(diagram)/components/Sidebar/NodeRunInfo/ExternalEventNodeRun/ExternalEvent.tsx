import { ExternalEvent as ExternalEventProto } from 'littlehorse-client/proto'
import { NodeVariable } from '../NodeVariable'
import { getVariableValue } from '@/app/utils'
import { VARIABLE_TYPES } from '@/app/constants'
import { Divider } from '../../Components/Divider'

export const ExternalEvent = ({ event }: { event: ExternalEventProto }) => {
  const variable = event.content
  const variableType = variable?.value?.$case
  return (
    <>
      <Divider title="ExternalEvent" />
      <NodeVariable label="externalEventId:" text={`${event.id?.externalEventDefId?.name}`} />
      <NodeVariable label="guid:" text={`${event.id?.guid}`} />
      <NodeVariable label="wfRunId:" text={`${event.id?.wfRunId?.id}`} />
      <NodeVariable label="createdAt:" text={`${event.createdAt}`} />
      {event.threadRunNumber !== undefined && (
        <NodeVariable label="threadRunNumber:" text={`${event.threadRunNumber}`} />
      )}
      {event.nodeRunPosition && <NodeVariable label="nodeRunPosition:" text={`${event.nodeRunPosition}`} />}
      <NodeVariable label="claimed:" text={`${event.claimed ? 'Yes' : 'No'}`} />
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
    </>
  )
}
