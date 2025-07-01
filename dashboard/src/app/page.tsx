import { lhClient } from '@/utils/client/lhClient'
import { redirect } from 'next/navigation'

export default async function HomePage() {
  const client = await lhClient()
  const tenants = await client.searchTenant({ limit: 12_01_24 })

  if (tenants.results.length === 0) {
    return <div>No tenants found for your authenticated Principal. Please contact your administrator.</div>
  }

  redirect(`/${tenants.results[0].id}`)
}
