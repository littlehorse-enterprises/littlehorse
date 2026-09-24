import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { ExternalEventDef } from './components/ExternalEventDef'
import { getExternalEventDef } from './getExternalEventDef'

type Props = { params: Promise<{ name: string; tenantId: string }> }

export default async function Page({ params }: Props) {
  const { name, tenantId } = await params

  try {
    const spec = await getExternalEventDef(tenantId, { name })
    return <ExternalEventDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { name } = await params

  return {
    title: `ExternalEventDef ${name} | Littlehorse`,
  }
}
