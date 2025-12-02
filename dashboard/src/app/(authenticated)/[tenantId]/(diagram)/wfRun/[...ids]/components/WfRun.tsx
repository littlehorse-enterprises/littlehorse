'use client'
import { Diagram } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Diagram'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { getWfRun, WfRunResponse } from '@/app/actions/getWfRun'
import { wfRunIdToPath } from '@/app/utils'
import { Separator } from '@/components/ui/separator'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { FC, useMemo } from 'react'
import useSWR from 'swr'
import { ChildWorkflows } from './ChildWorkflows'
import { Details } from './Details'
import { Variables } from './Variables'

export const WfRun: FC<WfRunResponse> = wfRunData => {
  const { tenantId } = useWhoAmI()
  const wfRunId = wfRunData.wfRun.id
  if (!wfRunId) return

  const { data } = useSWR(
    `wfRun/${tenantId}/${wfRunIdToPath(wfRunId)}`,
    async () => {
      return await getWfRun({ wfRunId, tenantId })
    },
    { fallbackData: wfRunData }
  )

  const { wfSpec, wfRun, variables } = data

  const variableDefs = useMemo(() => {
    if (!wfSpec) return []
    const { threadSpecName } = wfRun.threadRuns[wfRun.greatestThreadrunNumber]
    return wfSpec.threadSpecs[threadSpecName].variableDefs
  }, [data])

  return (
    <div className="mb-16">
      <Navigation
        href={`/wfSpec/${wfRun.wfSpecId?.name}/${wfRun.wfSpecId?.majorVersion}/${wfRun.wfSpecId?.revision}`}
        title="Go back to WfSpec"
      />
      <Details {...wfRun} />
      <Diagram spec={wfSpec} wfRun={wfRun} />

      {wfRun.id && (
        <>
          <Variables variableDefs={variableDefs} variables={variables} />
          <Separator className="mb-4 mt-4" />
          <ChildWorkflows parentWfRunId={wfRun.id} spec={wfSpec} />
        </>
      )}
    </div>
  )
}
