import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WorkflowEventDef } from './components/WorkflowEventDef'
import { getWorkflowEventDef } from './getWorkflowEventDef'

type Props = { params: Promise<{ name: string; tenantId: string }> }

export default async function Page(props: Props) {
  const params = await props.params;

  const {
    name,
    tenantId
  } = params;

  try {
    const spec = await getWorkflowEventDef(tenantId, { name })
    return <WorkflowEventDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata(props: Props): Promise<Metadata> {
  const params = await props.params;

  const {
    name
  } = params;

  return {
    title: `WorkflowEventDef ${name} | Littlehorse`,
  }
}
