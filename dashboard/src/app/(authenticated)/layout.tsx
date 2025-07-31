import { Toaster } from '@/components/ui/sonner'
import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import { SWRConfig } from 'swr'
import getWhoAmI from '../getWhoami'
import '../globals.css'
import { Header } from './[tenantId]/components/Header'
import { QueryProvider } from './[tenantId]/components/QueryProvider'
import { PropsWithChildren } from 'react'
import { WithTenant } from '@/types'

export default async function RootLayout({
  children,
  params: { tenantId },
}: PropsWithChildren<{ params: WithTenant }>) {
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
          <div className="mx-auto max-w-screen-xl px-8">{children}</div>
          <Toaster position="top-center" richColors />
        </QueryProvider>
      </WhoAmIContext>
    </SWRConfig>
  )
}
