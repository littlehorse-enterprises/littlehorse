'use client'
import { Navigation } from '@/app/(authenticated)/[tenantId]/components/Navigation'
import { ScheduledWfRunIdList, WfSpec as Spec } from 'littlehorse-client/proto'
import { LucidePlayCircle } from 'lucide-react'
import { FC, useCallback, useState } from 'react'
import { Diagram } from '../../../components/Diagram'
import { useModal } from '../../../hooks/useModal'
import { Details } from './Details'
import { Thread } from './Thread'
import { WfRuns } from './WfRuns'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScheduledWfRuns } from './ScheduledWfRuns'
import { ScheduledWfRun } from 'littlehorse-client/proto'
import { useWfSpec } from '@/app/hooks/useWfSpec'
import { useParams } from 'next/navigation'

type WfSpecProps = {
  spec: Spec
}
export const WfSpec: FC<WfSpecProps> = ({ spec }) => {
  const tenantId = useParams().tenantId as string
  const { wfSpec, isLoading, isError } = useWfSpec(tenantId, spec.id!.name, spec.id!.majorVersion.toString(), spec.id!.revision.toString())

  const { setModal, setShowModal } = useModal()

  const onClick = useCallback(() => {
    if (!spec) return
    setModal({ type: 'workflowRun', data: { ...spec } })
    setShowModal(true)
  }, [spec, setModal, setShowModal])
  return (
    <>
      <Navigation href="/" title="Go back to WfSpecs" />
      <div className="flex items-center justify-between">
        <Details status={spec.status} id={spec.id} />
        <button className="flex items-center gap-1 rounded-sm bg-blue-500 p-2 px-4 text-white" onClick={onClick}>
          <LucidePlayCircle size={18} />
          Execute
        </button>
      </div>
      <Diagram spec={spec} />
      {Object.keys(spec.threadSpecs)
        .reverse()
        .map(name => (
          <Thread key={name} name={name} spec={spec.threadSpecs[name]} />
        ))}

      <Tabs defaultValue="runs">
        <TabsList>
          <TabsTrigger value="runs">WfRuns</TabsTrigger>
          <TabsTrigger value="schedule">ScheduledWfRuns</TabsTrigger>
        </TabsList>
        <TabsContent value="runs">
          <WfRuns {...spec} />
        </TabsContent>
        <TabsContent value="schedule">
          <ScheduledWfRuns {...spec} />
        </TabsContent>
      </Tabs>
    </>
  )
}
