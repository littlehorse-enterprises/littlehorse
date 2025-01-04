import { Toaster } from '@/components/ui/sonner'
import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import { Header } from './[tenantId]/components/Header'
import { QueryProvider } from './[tenantId]/components/QueryProvider'
import getWhoAmI from './getWhoami'
import './globals.css'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'LittleHorse | Dashboard',
}

export default async function RootLayout(
  props: Readonly<{
    children: React.ReactNode
    params?: { tenantId?: string }
  }>
) {
  const params = await props.params;

  const {
    children
  } = props;

  const { tenants, user } = await getWhoAmI()

  return (
    <html lang="en">
      <body className={inter.className}>
        <WhoAmIContext user={user} tenants={tenants} tenantId={params?.tenantId}>
          <Header />
          <QueryProvider>
            <div className="mx-auto max-w-screen-xl px-8">{children}</div>
            <Toaster position="top-center" richColors />
          </QueryProvider>
        </WhoAmIContext>
      </body>
    </html>
  )
}
