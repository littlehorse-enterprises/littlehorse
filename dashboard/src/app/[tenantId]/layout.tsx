import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { cookies } from 'next/headers'
import getWhoAmI from '../getWhoami'
import { Header } from './components/Header'
import { QueryProvider } from './components/QueryProvider'
import { redirect } from 'next/navigation'
import { setTenant } from '@/setTenant'
import { useEffect } from 'react'

export const metadata: Metadata = {
  title: 'LittleHorse | Dashboard',
}

export default async function RootLayout({
  children,
  params,
}: Readonly<{
  children: React.ReactNode
  params: { tenantId: string }
}>) {
  const { tenants, user } = await getWhoAmI()

  return (
    <WhoAmIContext user={user} tenants={tenants} tenantId={params.tenantId}>
      <Header />
      <QueryProvider>
        <div className="mx-auto max-w-screen-xl px-8">{children}</div>
      </QueryProvider>
    </WhoAmIContext>
  )
}
