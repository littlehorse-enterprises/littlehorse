'use client'

import { WF_SPEC_STATUS } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Sidebar/Components/StatusColor'
import { formatDateReadable } from '@/app/utils'
import { cn } from '@/components/utils'
import { Separator } from '@/components/ui/separator'
import { useQuery } from '@tanstack/react-query'
import { WfSpec } from 'littlehorse-client/proto'
import { RefreshCwIcon } from 'lucide-react'
import { useParams } from 'next/navigation'
import { FC, ReactNode } from 'react'
import { countNodeRun } from '../actions/countNodeRun'
import { Versions } from './Versions'

type Props = {
  spec: WfSpec
  actions?: ReactNode
}

const MetadataField: FC<{ label: string; children: ReactNode }> = ({ label, children }) => (
  <div className="rounded-md border border-gray-200 bg-white px-4 py-2.5">
    <p className="text-xs text-muted-foreground">{label}</p>
    <p className="mt-1 text-sm font-medium leading-snug">{children}</p>
  </div>
)

const formatCount = (value: number) => value.toLocaleString()

const formatRetention = (seconds: number) => {
  if (seconds < 60) return `${seconds}s after termination`
  if (seconds < 3600) return `${Math.round(seconds / 60)}m after termination`
  if (seconds < 86400) return `${Math.round(seconds / 3600)}h after termination`
  const days = Math.round(seconds / 86400)
  return `${days}d after termination`
}

const getRetentionSeconds = (spec: WfSpec) =>
  spec.retentionPolicy?.wfGcPolicy?.$case === 'secondsAfterWfTermination'
    ? Number(spec.retentionPolicy.wfGcPolicy.value)
    : undefined

export const WfSpecMetadata: FC<Props> = ({ spec, actions }) => {
  const tenantId = useParams().tenantId as string
  const retentionSeconds = getRetentionSeconds(spec)
  const { backgroundColor, textColor, Icon, animate } = WF_SPEC_STATUS[spec.status]

  const { data, isPending } = useQuery({
    queryKey: ['wfSpecNodeRunCount', tenantId, spec.id?.name, spec.id?.majorVersion, spec.id?.revision],
    queryFn: () => countNodeRun({ tenantId, wfSpecId: spec.id! }),
    enabled: spec.id !== undefined,
    refetchInterval: 30_000,
  })

  return (
    <div className="mb-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <span className="text-sm italic text-muted-foreground">WfSpec</span>
          <h1 className="mt-1 text-2xl font-bold tracking-tight">{spec.id?.name}</h1>
          <span
            className={cn(
              'mt-1 inline-flex shrink-0 items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-medium',
              backgroundColor,
              textColor
            )}
          >
            {animate ? (
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-current opacity-60" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-current" />
              </span>
            ) : (
              <Icon className="h-3 w-3" />
            )}
            {spec.status}
          </span>
        </div>
        {actions}
      </div>

      <div className="mt-4 flex flex-wrap gap-3">
        <MetadataField label="Version">
          <Versions wfSpecId={spec.id} compact />
        </MetadataField>
        <MetadataField label="Created">
          {spec.createdAt ? formatDateReadable(spec.createdAt) : '—'}
        </MetadataField>
        <MetadataField label="Storage usage">
          {isPending ? (
            <RefreshCwIcon className="h-4 w-4 animate-spin text-muted-foreground" />
          ) : data?.status === 'ok' ? (
            <span className="tabular-nums">{formatCount(data.count)} NodeRuns</span>
          ) : (
            <span className="text-muted-foreground">{data?.message ?? 'Unavailable'}</span>
          )}
        </MetadataField>
        <MetadataField label="Retention">
          {retentionSeconds !== undefined ? (
            formatRetention(retentionSeconds)
          ) : (
            <span className="text-muted-foreground">None</span>
          )}
        </MetadataField>
      </div>
      <Separator className="mt-4" />
    </div>
  )
}
