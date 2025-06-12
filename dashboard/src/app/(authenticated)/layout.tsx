import { Toaster } from '@/components/ui/sonner'
import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import { SWRConfig } from 'swr'
import getWhoAmI from '../getWhoami'
import '../globals.css'
import { Header } from './[tenantId]/components/Header'
import { QueryProvider } from './[tenantId]/components/QueryProvider'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'LittleHorse | Dashboard',
}

export default async function RootLayout({
  children,
  params,
}: Readonly<{
  children: React.ReactNode
  params?: { tenantId?: string }
}>) {
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
      <WhoAmIContext user={user} tenants={tenants} tenantId={params?.tenantId}>
        <Header />
        <QueryProvider>
          <div className="mx-auto max-w-screen-xl px-8">{children}</div>
          <Toaster position="top-center" richColors />
        </QueryProvider>
      </WhoAmIContext>
    </SWRConfig>
  )
}
