import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/proto'
import { NodeVariable } from '../NodeVariable'
import { TypeDisplay } from '@/app/(authenticated)/[tenantId]/components/TypeDisplay'
import { Divider } from '../../Components/Divider'

export const ExternalEventDef = ({ event }: { event: ExternalEventDefProto }) => {
  return (
    <>
      <Divider title="ExternalEventDef" />
      <NodeVariable label="ExternalEventDefId:" text={`${event?.id?.name}`} />
      <NodeVariable label="createdAt:" text={`${event?.createdAt}`} type="date" />
      {event.retentionPolicy && Object.keys(event.retentionPolicy).length > 0 && (
        <NodeVariable label="retentionPolicy:" text={`${event?.retentionPolicy?.extEvtGcPolicy?.value}`} />
      )}
      {event?.typeInformation && (
        <div className="ml-1">
          <div className=" mb-1 text-sm font-bold"> typeInformation</div>
          <div className=" flex items-center gap-1">
            <span className="rounded	bg-gray-100 px-2  font-mono text-xs text-fuchsia-500">
              {event.typeInformation.returnType?.definedType?.$case}
            </span>
            <TypeDisplay definedType={event.typeInformation.returnType?.definedType} />
            <p className="rounded bg-blue-300 p-1 text-xs ">
              {event.typeInformation.returnType?.masked ? ' Masked' : 'Unmasked'}
            </p>
          </div>
        </div>
      )}
      {event?.correlatedEventConfig && (
        <NodeVariable label="ttlSeconds:" text={`${event?.correlatedEventConfig?.ttlSeconds}`} />
      )}
      <NodeVariable
        label="deleteAfterFC:"
        text={`${event?.correlatedEventConfig?.deleteAfterFirstCorrelation ? 'Yes' : 'No'}`}
      />
    </>
  )
}
