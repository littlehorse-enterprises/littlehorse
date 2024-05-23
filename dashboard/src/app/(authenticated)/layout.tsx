import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { cookies } from 'next/headers'
import getWhoAmI from '../getWhoami'
import { Header } from './components/Header'
import { QueryProvider } from './components/QueryProvider'

export const metadata: Metadata = {
  title: 'LittleHorse | Dashboard',
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  const { tenants, user } = await getWhoAmI()
  const tenantId = cookies().get('tenantId')?.value

  return (
    <WhoAmIContext user={user} tenants={tenants} tenantId={tenantId}>
      <Header />
      <QueryProvider>
        <div className="px-8 mx-auto max-w-screen-xl">{children}</div>
      </QueryProvider>
    </WhoAmIContext>
  )
}
