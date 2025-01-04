import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfRun } from './components/WfRun'
import { getWfRun } from './getWfRun'

type Props = { params: Promise<{ ids: string[]; tenantId: string }> }

export default async function Page(props: Props) {
  const params = await props.params;

  const {
    ids,
    tenantId
  } = params;

  try {
    return <WfRun {...await getWfRun({ ids, tenantId })} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata(props: Props): Promise<Metadata> {
  const params = await props.params;

  const {
    ids
  } = params;

  return {
    title: `WfRun ${ids[ids.length - 1]} | Littlehorse`,
  }
}
