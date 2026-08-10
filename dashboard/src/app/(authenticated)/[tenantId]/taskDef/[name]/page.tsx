import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { TaskDef } from './components/TaskDef'
import { getTaskDef } from './getTaskDef'

type Props = { params: Promise<{ name: string; tenantId: string }> }

export default async function Page({ params }: Props) {
  const { name, tenantId } = await params

  try {
    const spec = await getTaskDef(tenantId, { name })
    return <TaskDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { name } = await params

  return {
    title: `TaskDef ${name} | Littlehorse`,
  }
}
