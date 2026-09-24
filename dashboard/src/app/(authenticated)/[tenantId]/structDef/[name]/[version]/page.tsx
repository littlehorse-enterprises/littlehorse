import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { StructDefClient } from '../components/StructDef'
import { getStructDef } from '../getStructDef'

type Props = { params: Promise<{ name: string; version: string; tenantId: string }> }

export default async function Page({ params }: Props) {
  const { name, version, tenantId } = await params

  try {
    const structDef = await getStructDef(tenantId, { name, version: Number(version) })
    return <StructDefClient structDef={structDef} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { name, version } = await params

  return {
    title: `StructDef ${name} v${version} | Littlehorse`,
  }
}
