import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { getWfSpec } from './actions/getWfSpec'

import { WfSpec } from './components/WfSpec'

type Props = { params: { props: string[]; tenantId: string } }

export const dynamic = 'force-dynamic'

export default async function Page({ params: { props, tenantId } }: Props) {
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

export async function generateMetadata({ params: { props } }: Props): Promise<Metadata> {
  const name = props[0]

  return {
    title: `WfSpec ${name} | Littlehorse`,
  }
}
