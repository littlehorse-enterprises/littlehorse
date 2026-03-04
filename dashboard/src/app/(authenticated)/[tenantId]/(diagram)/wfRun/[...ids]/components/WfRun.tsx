'use client'
import { Diagram } from '@/app/(authenticated)/[tenantId]/(diagram)/components/Diagram'
import { ThreadType } from '@/app/(authenticated)/[tenantId]/(diagram)/context'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { getWfRun, WfRunResponse } from '@/app/actions/getWfRun'
import { wfRunIdToPath } from '@/app/utils'
import { Separator } from '@/components/ui/separator'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import { FC, useCallback, useMemo, useState } from 'react'
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

  const initialThread = useMemo<ThreadType>(() => {
    const tr = wfRun.threadRuns.find(t => t.number === wfRun.greatestThreadrunNumber)
    return {
      name: tr?.threadSpecName ?? wfSpec.entrypointThreadName,
      number: wfRun.greatestThreadrunNumber,
    }
  }, [wfRun.greatestThreadrunNumber, wfRun.threadRuns, wfSpec.entrypointThreadName])

  const [selectedThread, setSelectedThread] = useState<ThreadType>(initialThread)

  const onThreadChange = useCallback((thread: ThreadType) => {
    setSelectedThread(thread)
  }, [])

  const variableDefs = useMemo(() => {
    if (!wfSpec) return []
    const threadSpec = wfSpec.threadSpecs[selectedThread.name]
    if (!threadSpec) return []
    return threadSpec.variableDefs ?? []
  }, [wfSpec, selectedThread.name])

  const wfSpecUrl = useMemo(() => {
    const { name, majorVersion, revision } = wfRun.wfSpecId ?? {}
    return `/wfSpec/${name}/${majorVersion}/${revision}`
  }, [wfRun.wfSpecId])

  return (
    <div className="mb-16">
      <Navigation href={wfSpecUrl} title="Go back to WfSpec" />

      <Details {...wfRun} />

      <Diagram spec={wfSpec} wfRun={wfRun} onThreadChange={onThreadChange} />

      {wfRun.id && (
        <>
          <Variables variableDefs={variableDefs} variables={variables} thread={selectedThread} wfRunId={wfRun.id} />
          <Separator className="mb-4 mt-4" />
          <ChildWorkflows parentWfRunId={wfRun.id} spec={wfSpec} />
        </>
      )}
    </div>
  )
}
