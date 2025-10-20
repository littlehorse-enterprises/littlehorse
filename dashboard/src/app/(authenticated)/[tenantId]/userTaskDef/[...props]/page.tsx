import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { UserTaskDef } from './components/UserTaskDef'
import { getUserTaskDef } from './getUserTaskDef'

type Props = { params: { props: string[]; tenantId: string } }

export default async function Page({ params: { props, tenantId } }: Props) {
  const name = props[0]
  const version = props[1]
  try {
    const spec = await getUserTaskDef({ name, version, tenantId })
    return <UserTaskDef spec={spec} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { props } }: Props): Promise<Metadata> {
  const name = props[0]

  return {
    title: `UserTaskDef ${name} | Littlehorse`,
  }
}
