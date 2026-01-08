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

  if (!wfRunId) {
    return null
  }

  const swrKey = `wfRun/${tenantId}/${wfRunIdToPath(wfRunId)}`

  const { data } = useSWR(swrKey, async () => await getWfRun({ wfRunId, tenantId }), { fallbackData: wfRunData })
  const { wfSpec, wfRun, variables } = data

  const variableDefs = useMemo(() => {
    if (!wfSpec || !wfRun.threadRuns?.length) return []
    const greatestThreadRun = wfRun.threadRuns[wfRun.greatestThreadrunNumber]
    const threadSpec = wfSpec.threadSpecs[greatestThreadRun.threadSpecName]

    return threadSpec?.variableDefs ?? []
  }, [wfSpec, wfRun.greatestThreadrunNumber, wfRun.threadRuns])

  const wfSpecUrl = useMemo(() => {
    const { name, majorVersion, revision } = wfRun.wfSpecId ?? {}
    return `/wfSpec/${name}/${majorVersion}/${revision}`
  }, [wfRun.wfSpecId])

  return (
    <div className="mb-16">
      <Navigation href={wfSpecUrl} title="Go back to WfSpec" />

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
