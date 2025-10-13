import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ClientError, Status } from 'nice-grpc-common'
import { StructDefClient } from '../components/StructDef'
import { getStructDef } from '../getStructDef'

type Props = { params: { name: string; version: string; tenantId: string } }

export default async function Page({ params: { name, version, tenantId } }: Props) {
  try {
    const structDef = await getStructDef(tenantId, { name, version: Number(version) })
    return <StructDefClient structDef={structDef} />
  } catch (error) {
    if (error instanceof ClientError && error.code === Status.NOT_FOUND) return notFound()
    throw error
  }
}

export async function generateMetadata({ params: { name, version } }: Props): Promise<Metadata> {
  return {
    title: `StructDef ${name} v${version} | Littlehorse`,
  }
}
