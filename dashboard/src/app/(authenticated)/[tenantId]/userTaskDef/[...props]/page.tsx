import { getStructDef } from '@/app/actions/getStructDef'
import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { UserTaskDef } from './components/UserTaskDef'
import { getUserTaskDef } from './getUserTaskDef'

type Props = { params: Promise<{ props: string[]; tenantId: string }> }

export default async function Page({ params }: Props) {
  const { props, tenantId } = await params
  const name = props[0]
  const version = props[1]

  try {
    const spec = await getUserTaskDef({ name, version, tenantId })
    // The page is still useful without the fields, e.g. when the StructDef was deleted.
    const resultStructDef = spec.resultStructDefId
      ? await getStructDef(tenantId, spec.resultStructDefId).catch(() => undefined)
      : undefined
    return <UserTaskDef spec={spec} resultStructDef={resultStructDef} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { props } = await params
  const name = props[0]

  return {
    title: `UserTaskDef ${name} | Littlehorse`,
  }
}
