import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { getWfSpec } from './actions/getWfSpec'
import { getScheduleWfSpec } from './actions/getScheduleWfSpec'
import { WfSpec } from './components/WfSpec'

type Props = { params: Promise<{ props: string[]; tenantId: string }> }

export const dynamic = 'force-dynamic'

export default async function Page(props0: Props) {
  const params = await props0.params;

  const {
    props,
    tenantId
  } = params;

  const name = props[0]
  const version = props[1]

  try {
    const wfSpec = await getWfSpec({ tenantId, name, version })
    return <WfSpec spec={wfSpec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata(props0: Props): Promise<Metadata> {
  const params = await props0.params;

  const {
    props
  } = params;

  const name = props[0]

  return {
    title: `WfSpec ${name} | Littlehorse`,
  }
}
