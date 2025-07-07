import { getWfRunDetails, WfRunDetails } from '@/actions/getWfRunDetails'
import { lhClient } from '@/lhClient'
import { extractEdges } from '@/utils/data/extract-edges'
import { extractNodes } from '@/utils/data/extract-nodes'
import DiagramClient from '@/components/diagram/DiagramClient'

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
    <DiagramClient
      wfSpec={wfSpec}
      wfRunDetails={wfRunDetails}
      nodes={nodes}
      edges={edges}
    />
  )
}
