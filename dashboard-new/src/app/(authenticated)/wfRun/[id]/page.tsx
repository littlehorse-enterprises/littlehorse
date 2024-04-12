import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'
import { getWfRun } from './getWfRun'

type Props = { params: { id: string } }

export default async function Page({ params: { id } }: Props) {
  try {
    const { wfRun, wfSpec, nodeRuns } = await getWfRun({ id })
    return <WfRun wfRun={wfRun} wfSpec={wfSpec} nodeRuns={nodeRuns} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { id } }: Props): Promise<Metadata> {
  return {
    title: `WfRun ${id} | Littlehorse`,
  }
}
