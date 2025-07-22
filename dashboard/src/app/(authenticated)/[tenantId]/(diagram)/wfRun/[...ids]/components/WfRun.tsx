'use client'
import { Diagram } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Diagram'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { useWfRun } from '@/app/hooks/useWfRun'
import { useSearchParams } from 'next/navigation'
import { FC } from 'react'
import { Details } from './Details'
import { Variables } from './Variables'
export const WfRun: FC<{ id: string; tenantId: string }> = ({ id, tenantId }) => {
  const searchParams = useSearchParams()
  const threadRunNumber = Number(searchParams.get('threadRunNumber'))
  const { wfRunData, isLoading, isError } = useWfRun({ wfRunId: { id }, tenantId })

  if (!wfRunData) return null
  const { wfRun, wfSpec, nodeRuns, variables } = wfRunData

  if (!wfRun) return null

  return (
    <div className="mb-16">
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} nodeRuns={nodeRuns} />

      <Variables
        variableDefs={wfSpec.threadSpecs[wfRun.threadRuns[threadRunNumber].threadSpecName].variableDefs}
        variables={variables.filter(v => v.id?.threadRunNumber == Number(searchParams.get('threadRunNumber')))}
      />
    </div>
  )
}
