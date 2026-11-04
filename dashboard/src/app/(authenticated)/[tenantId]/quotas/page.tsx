import { Metadata } from 'next'
import { getApplicableQuotas } from './actions/getApplicableQuotas'
import { QuotaUsage } from './components/QuotaUsage'

type Props = { params: Promise<{ tenantId: string }> }

export default async function Page({ params }: Props) {
  const { tenantId } = await params
  const quotas = await getApplicableQuotas({ tenantId })
  return <QuotaUsage quotas={quotas} />
}

export const metadata: Metadata = {
  title: 'Quota Usage | Littlehorse',
}
