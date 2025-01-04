import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { UserTaskDef } from './components/UserTaskDef'
import { getUserTaskDef } from './getUserTaskDef'

type Props = { params: Promise<{ props: string[] }> }

export default async function Page(props0: Props) {
  const params = await props0.params;

  const {
    props
  } = params;

  const name = props[0]
  const version = props[1]
  try {
    const spec = await getUserTaskDef({ name, version })
    return <UserTaskDef spec={spec} />
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
    title: `UserTaskDef ${name} | Littlehorse`,
  }
}
