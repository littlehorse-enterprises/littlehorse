import { getWfRun } from '@/app/actions/getWfRun'
import { wfRunIdFromList } from '@/app/utils'
import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'

type Props = { params: { ids: string[]; tenantId: string } }

export default async function Page({ params: { ids, tenantId } }: Props) {
  try {
    const wfRunId = wfRunIdFromList(ids)
    const wfRun = await getWfRun({ wfRunId, tenantId })
    return <WfRun {...wfRun} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { ids } }: Props): Promise<Metadata> {
  return {
    title: `WfRun ${ids[ids.length - 1]} | Littlehorse`,
  }
}
