import { getWfRunDetails } from '@/actions/getWfRun'
import { extractEdgeData, extractNodeData } from '@/utils/data/data-extraction'
import { lhClient } from '@/utils/client/lhClient'
import { type Edge, type Node } from '@xyflow/react'
import { SelectionProvider } from '@/components/context/selection-context'
import { WfRunDetails } from '@/types/wfRunDetails'
import LeftSidebar from '@/components/diagram/left-sidebar/left-sidebar'
import WorkflowDiagram from '@/components/diagram/workflow-diagram'
import RightSidebar from '@/components/diagram/right-sidebar'

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

  const nodes: Node[] = extractNodeData(wfSpec, wfRunDetails)
  const edges: Edge[] = extractEdgeData(wfSpec)

  return (
    <SelectionProvider>
      <div className="flex h-full">
        <LeftSidebar wfSpec={wfSpec} wfRun={wfRunDetails?.wfRun} />
        <div className="flex flex-1">
          <WorkflowDiagram nodes={nodes} edges={edges} />
          <RightSidebar
            wfSpec={wfSpec}
            nodeRuns={wfRunDetails?.nodeRuns || []}
            taskRuns={wfRunDetails?.taskRuns || []}
          />
        </div>
      </div>
    </SelectionProvider>
  )
}
