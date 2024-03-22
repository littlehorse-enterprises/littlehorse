import { Metadata } from 'next'
import { cookies } from 'next/headers'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { WfSpec } from './components/WfSpec'
import { getWfSpec } from './getWfSpec'

type Props = { params: { props: string[] } }

export default async function Page({ params: { props } }: Props) {
  const name = props[0]
  const version = props[1]
  const tenantId = cookies().get('tenantId')?.value
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
