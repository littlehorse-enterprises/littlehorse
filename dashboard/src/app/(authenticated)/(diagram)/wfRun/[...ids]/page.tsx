import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'
import { getWfRun } from './getWfRun'

type Props = { params: { ids: string[] } }

export default async function Page({ params: { ids } }: Props) {
  try {
    const { wfRun, wfSpec, nodeRuns } = await getWfRun({ ids })
    return <WfRun wfRun={wfRun} wfSpec={wfSpec} nodeRuns={nodeRuns} />
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
