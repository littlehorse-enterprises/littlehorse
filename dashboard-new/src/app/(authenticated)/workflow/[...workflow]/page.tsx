import { cookies } from 'next/headers'
import { getWfSpec } from './getWfSpec'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { Workflow } from './components/Workflow'
import { Metadata, ResolvingMetadata } from 'next'

type Props = { params: { workflow: string[] } }

export default async function Page({ params: { workflow } }: Props) {
  const name = workflow[0]
  const version = workflow[1]
  const tenantId = cookies().get('tenantId')?.value
  try {
    const wfSpec = await getWfSpec({ tenantId, name, version })
    return <Workflow spec={wfSpec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { workflow } }: Props): Promise<Metadata> {
  const name = workflow[0]

  return {
    title: `Workflow ${name} | Littlehorse`,
  }
}
