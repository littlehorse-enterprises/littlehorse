'use client'
import { Diagram } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Diagram'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { useWfRun } from '@/app/hooks/useWfRun'
import { Separator } from '@/components/ui/separator'
import { WfRunId } from 'littlehorse-client/proto'
import { useSearchParams } from 'next/navigation'
import { FC } from 'react'
import ChildWorkflows from './ChildWorkflows'
import { Details } from './Details'
import { Variables } from './Variables'

export const WfRun: FC<{ ids: string[]; tenantId: string }> = ({ ids, tenantId }) => {
  const wfRunId = ids.reduce((wfRunId, id, i) => (i === 0 ? { id } : { id, parentWfRunId: wfRunId }), {} as WfRunId)

  const searchParams = useSearchParams()
  const threadRunNumber = Number(searchParams.get('threadRunNumber'))
  const { wfRunData } = useWfRun({ wfRunId, tenantId })

  if (!wfRunData) return null
  const { wfRun, wfSpec, nodeRuns, variables } = wfRunData

  if (!wfRun) return null

  const variableDefs = wfSpec.threadSpecs[wfRun.threadRuns[threadRunNumber].threadSpecName].variableDefs

  return (
    <div className="mb-16">
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} nodeRuns={nodeRuns} />

      <Variables
        variableDefs={variableDefs}
        variables={variables.filter(v => v.id?.threadRunNumber == Number(searchParams.get('threadRunNumber')))}
      />

      {wfRun.id && (
        <>
          <Separator className="mb-4 mt-4" />
          <ChildWorkflows parentWfRunId={wfRun.id} spec={wfSpec} />
        </>
      )}
    </div>
  )
}
