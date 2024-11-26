import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { TaskDef } from './components/TaskDef'
import { getTaskDef } from './getTaskDef'

type Props = { params: { name: string; tenantId: string } }

export default async function Page({ params: { name, tenantId } }: Props) {
  try {
    const spec = await getTaskDef(tenantId, { name })
    return <TaskDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { name } }: Props): Promise<Metadata> {
  return {
    title: `TaskDef ${name} | Littlehorse`,
  }
}
