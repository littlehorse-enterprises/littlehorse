import { lhClient } from '@/app/lhClient'
import { WfRunId } from 'littlehorse-client/proto'
import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'

type Props = { params: { ids: string[]; tenantId: string } }

async function getInheritedVariables(wfRunId: WfRunId, client: Awaited<ReturnType<typeof lhClient>>) {
  const variables = []
  client.getVariable({ wfRunId: wfRunId, threadRunNumber: 0 })
}

export default async function Page({ params: { ids, tenantId } }: Props) {
  try {
    return <WfRun ids={ids} tenantId={tenantId} />
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
