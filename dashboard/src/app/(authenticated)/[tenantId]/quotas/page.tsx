import { Metadata } from 'next'
import { getQuotas } from './actions/getQuotas'
import { QuotaUsage } from './components/QuotaUsage'

type Props = {
  params: Promise<{ tenantId: string }>
  searchParams: Promise<{ quota?: string }>
}

export default async function Page({ params, searchParams }: Props) {
  const { tenantId } = await params
  const { quota } = await searchParams
  const { quotas, quotasAvailable } = await getQuotas({ tenantId })
  return <QuotaUsage quotas={quotas} quotasAvailable={quotasAvailable} initialQuotaKey={quota} />
}

export const metadata: Metadata = {
  title: 'Quota Usage | Littlehorse',
}
