import { Toaster } from '@/components/ui/sonner'
import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import { Header } from './[tenantId]/components/Header'
import { QueryProvider } from './[tenantId]/components/QueryProvider'
import getWhoAmI from '../getWhoami'
import '../globals.css'

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
    <WhoAmIContext user={user} tenants={tenants} tenantId={params?.tenantId}>
      <Header />
      <QueryProvider>
        {children}
        <Toaster position="top-center" richColors />
      </QueryProvider>
    </WhoAmIContext>
  )
}
