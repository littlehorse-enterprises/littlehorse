import { getWfRun } from '@/app/actions/getWfRun'
import { wfRunIdFromList } from '@/app/utils'
import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'

type Props = { params: Promise<{ ids: string[]; tenantId: string }> }

export default async function Page({ params }: Props) {
  const { ids, tenantId } = await params

  try {
    const wfRunId = wfRunIdFromList(ids)
    const wfRun = await getWfRun({ wfRunId, tenantId })
    return <WfRun {...wfRun} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { ids } = await params

  return {
    title: `WfRun ${ids[ids.length - 1]} | Littlehorse`,
  }
}
