'use client'
import { Breadcrumb } from '@/app/(authenticated)/[tenantId]/components/Breadcrumb'
import { DiagramProvider, NodeInContext } from '@/app/(authenticated)/[tenantId]/(diagram)/context'
import { routes } from '@/app/routes'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Separator } from '@/components/ui/separator'
import { WfSpec as Spec } from 'littlehorse-client/proto'
import { LucidePlayCircle } from 'lucide-react'
import { FC, useCallback, useState } from 'react'
import { Diagram } from '../../../components/Diagram'
import { useModal } from '../../../hooks/useModal'
import { ScheduledWfRuns } from './ScheduledWfRuns'
import { ThreadsSection } from './ThreadsSection'
import { WfRuns } from './WfRuns'
import { WfSpecMetrics } from './metrics'
import { WfSpecMetadata } from './WfSpecMetadata'

type WfSpecProps = {
  spec: Spec
}
export const WfSpec: FC<WfSpecProps> = ({ spec }) => {
  const { setModal, setShowModal } = useModal()
  const [thread, setThread] = useState({ name: spec.entrypointThreadName, number: 0 })
  const [selectedNode, setSelectedNode] = useState<NodeInContext>(undefined)

  const onClick = useCallback(() => {
    if (!spec) return
    setModal({ type: 'workflowRun', data: { ...spec } })
    setShowModal(true)
  }, [spec, setModal, setShowModal])

  const executeButton = (
    <button
      type="button"
      className="flex items-center gap-1 rounded-sm bg-blue-500 p-2 px-4 text-white hover:bg-blue-600"
      onClick={onClick}
    >
      <LucidePlayCircle size={18} />
      Execute
    </button>
  )

  return (
    <>
      <Breadcrumb
        items={[
          { label: 'WfSpecs', href: routes.appRoot() },
          { label: spec.id?.name ?? '' },
        ]}
      />
      <WfSpecMetadata spec={spec} actions={executeButton} />
      <DiagramProvider value={{ thread, setThread, selectedNode, setSelectedNode }}>
        <Diagram spec={spec} />
      </DiagramProvider>
      {spec.id && (
        <div className="mb-12">
          <WfSpecMetrics wfSpecId={spec.id} />
        </div>
      )}
      <ThreadsSection spec={spec} />

      <section className="mb-8">
        <h2 className="text-sm font-medium text-muted-foreground">Workflow runs</h2>
        <Separator className="mt-2" />
        <Tabs defaultValue="runs" className="mt-4">
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
      </section>
    </>
  )
}
