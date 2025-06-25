import { getWfRunDetails, WfRunDetails } from '@/actions/getWfRunDetails'
import { NodeSelectionProvider } from '@/components/context/selection-context'
import LeftSidebar from '@/components/diagram/left-sidebar'
import RightSidebar from '@/components/diagram/right-sidebar'
import WorkflowDiagram from '@/components/diagram/workflow-diagram'
import { lhClient } from '@/utils/client/lhClient'
import { extractEdges } from '@/utils/data/extract-edges'
import { extractNodes } from '@/utils/data/extract-nodes'

interface DiagramPageProps {
  params: Promise<{
    tenantId: string
    wfSpecName: string
    wfSpecVersion: string
  }>
  searchParams: Promise<{
    wfRunId?: string
  }>
}

export default async function DiagramPage({ params, searchParams }: DiagramPageProps) {
  const { tenantId, wfSpecName, wfSpecVersion } = await params
  const { wfRunId } = await searchParams

  const client = await lhClient(tenantId)
  const splitVersion = wfSpecVersion.split('.')
  const wfSpec = await client.getWfSpec({
    name: wfSpecName,
    majorVersion: parseInt(splitVersion[0]),
    revision: parseInt(splitVersion[1]),
  })

  let wfRunDetails: WfRunDetails | undefined
  if (wfRunId) {
    wfRunDetails = await getWfRunDetails({ wfRunId: { id: wfRunId }, tenantId })
  }

  const nodes = extractNodes(wfSpec, wfSpec.threadSpecs[wfSpec.entrypointThreadName])
  const edges = extractEdges(wfSpec)

  return (
    <NodeSelectionProvider>
      <div className="flex h-full">
        <LeftSidebar wfSpec={wfSpec} wfRun={wfRunDetails?.wfRun} />
        <div className="flex flex-1">
          <WorkflowDiagram nodes={nodes} edges={edges} />
          <RightSidebar
            wfSpec={wfSpec}
            wfRunDetails={wfRunDetails}
          />
        </div>
      </div>
    </NodeSelectionProvider>
  )
}
