import { ExternalEventDef as ExternalEventDefProto } from 'littlehorse-client/proto'
import { IdentifierBadge, MaskedBadge } from '@/components/ui/badge'
import { NodeVariable } from '../../Components/NodeVariable'
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
            {event.typeInformation.returnType?.definedType?.$case && (
              <IdentifierBadge name={event.typeInformation.returnType.definedType.$case} />
            )}
            <TypeDisplay definedType={event.typeInformation.returnType?.definedType} />
            {event.typeInformation.returnType?.masked && <MaskedBadge />}
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
