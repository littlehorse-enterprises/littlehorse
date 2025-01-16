import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WorkflowEventDef } from './components/WorkflowEventDef'
import { getWorkflowEventDef } from './getWorkflowEventDef'

type Props = { params: { name: string; tenantId: string } }

export default async function Page({ params: { name, tenantId } }: Props) {
  try {
    const spec = await getWorkflowEventDef(tenantId, { name })
    return <WorkflowEventDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { name } }: Props): Promise<Metadata> {
  return {
    title: `WorkflowEventDef ${name} | Littlehorse`,
  }
}
