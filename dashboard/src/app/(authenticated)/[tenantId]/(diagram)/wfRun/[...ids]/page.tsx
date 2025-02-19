import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'
import { getWfRun } from '../../../../../actions/getWfRun'
import { WfRunId } from 'littlehorse-client/proto'

type Props = { params: { ids: string[]; tenantId: string } }

export default async function Page({ params: { ids, tenantId } }: Props) {
  const wfRunId: WfRunId = ids.reduce((wfRunId, id, i) => (i === 0 ? { id } : { id, parentWfRunId: wfRunId }), {} as WfRunId);

  try {
    return <WfRun wfRunId={wfRunId} tenantId={tenantId} />
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
