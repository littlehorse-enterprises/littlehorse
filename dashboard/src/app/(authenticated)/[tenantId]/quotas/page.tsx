import { Metadata } from 'next'
import { getApplicableQuota } from './actions/getApplicableQuota'
import { QuotaUsage } from './components/QuotaUsage'

type Props = { params: Promise<{ tenantId: string }> }

export default async function Page({ params }: Props) {
  const { tenantId } = await params
  const applicable = await getApplicableQuota({ tenantId })
  return <QuotaUsage {...applicable} />
}

export const metadata: Metadata = {
  title: 'Quota Usage | Littlehorse',
}
