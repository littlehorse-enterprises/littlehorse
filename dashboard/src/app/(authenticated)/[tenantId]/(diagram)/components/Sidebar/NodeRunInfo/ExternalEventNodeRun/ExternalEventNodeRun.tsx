import {
  ExternalEvent as ExternalEventProto,
  ExternalEventDef as ExternalEventDefProto,
  ExternalEventNodeRun as ExternalEventNodeRunProto,
} from 'littlehorse-client/proto'
import { FC } from 'react'
import { NodeVariable } from '../NodeVariable'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { getExternalEvent } from '../../../NodeTypes/ExternalEvent/actions'
import useSWR from 'swr'
import { getExternalEventDef } from '@/app/(authenticated)/[tenantId]/externalEventDef/[name]/getExternalEventDef'
import { ExternalEvent } from './ExternalEvent'
import { ExternalEventDef } from './ExternalEventDef'

export const ExternalEventNodeRun: FC<{ node: ExternalEventNodeRunProto }> = ({ node }) => {
  const { externalEventId, externalEventDefId } = node
  const { tenantId } = useWhoAmI()
  const key = externalEventId ? ['externalEvent', tenantId, externalEventId] : null
  const keyDef = externalEventDefId ? ['externalEventDef', tenantId, externalEventDefId] : null

  const { data: externalEventNode } = useSWR<ExternalEventProto | undefined>(key, async () => {
    if (!externalEventId) return undefined
    return getExternalEvent({ tenantId, ...externalEventId })
  })
  const { data: externalEventDefNode } = useSWR<ExternalEventDefProto | undefined>(keyDef, async () => {
    if (!externalEventDefId) return undefined
    return getExternalEventDef(tenantId, externalEventDefId)
  })
  return (
    <div>
      <NodeVariable label="Node Type:" text="External event " />
      {node.eventTime && <NodeVariable label="eventTime:" text={`${node.eventTime}`} />}
      <NodeVariable label="timedOut:" text={`${node.timedOut}`} />
      {node.correlationKey && <NodeVariable label="correlationKey:" text={`${node.correlationKey}`} />}
      {node.maskCorrelationKey && <NodeVariable label="correlationKey:" text={`it should be masked`} />}
      {externalEventDefNode && <ExternalEventDef event={externalEventDefNode} />}
      {externalEventNode && <ExternalEvent event={externalEventNode} />}
    </div>
  )
}
