'use client'
import { Diagram } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Diagram'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { useSearchParams } from 'next/navigation'
import { FC, useCallback } from 'react'
import { Details } from './Details'
import { Variables } from './Variables'
import { useWfRun } from '@/app/hooks/useWfRun'
import { WfRunVariableAccessLevel } from 'littlehorse-client/proto'
import { isExternal } from 'util/types'

export const WfRun: FC<{ id: string, tenantId: string }> = ({ id, tenantId }) => {
  const searchParams = useSearchParams()
  const threadRunNumber = Number(searchParams.get('threadRunNumber'))
  const { wfRunData, isLoading, isError } = useWfRun({ id: id, tenantId })
  const { wfRunData: parentWfRunData } = useWfRun({ id: wfRunData?.wfRun?.id?.parentWfRunId?.id ?? '', tenantId })

  if (!wfRunData) return null
  const { wfRun, wfSpec, nodeRuns, variables } = wfRunData;

  if (!wfRun) return null
  if (!wfRun.id?.parentWfRunId || !parentWfRunData) return null

  const variableDefs = wfSpec.threadSpecs[wfRun.threadRuns[threadRunNumber].threadSpecName].variableDefs;
  const inheritedVariables = variableDefs.filter(vD => vD.accessLevel === WfRunVariableAccessLevel.INHERITED_VAR).map(vD => {
    return parentWfRunData.variables.find(v => v.id?.name === vD.varDef?.name)!
  });

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
        variables={variables.filter(v => v.id?.threadRunNumber == Number(searchParams.get('threadRunNumber'))).concat(inheritedVariables)}
      />
    </div>
  )
}
