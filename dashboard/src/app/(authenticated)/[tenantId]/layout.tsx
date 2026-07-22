import { Toaster } from '@/components/ui/sonner'
import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import { WithTenant } from '@/types'
import { PropsWithChildren } from 'react'
import { SWRConfig } from 'swr'
import getWhoAmI from '../../getWhoami'
import { Header } from './components/Header'
import { QueryProvider } from './components/QueryProvider'

export default async function RootLayout({ children, params }: PropsWithChildren<{ params: Promise<WithTenant> }>) {
  const { tenantId } = await params
  const { tenants, user } = await getWhoAmI()

  return (
    <SWRConfig
      value={{
        refreshInterval: 3000,
        revalidateOnFocus: true,
        revalidateOnReconnect: true,
        revalidateOnMount: true,
        revalidateIfStale: true,
      }}
    >
      <WhoAmIContext user={user} tenants={tenants} tenantId={tenantId}>
        <Header />
        <QueryProvider>
          <div className="mx-auto px-8">{children}</div>
          <Toaster position="top-center" richColors />
        </QueryProvider>
      </WhoAmIContext>
    </SWRConfig>
  )
}
