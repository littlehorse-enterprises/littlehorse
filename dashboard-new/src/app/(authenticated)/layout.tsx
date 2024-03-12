import { WhoAmIContext } from '@/contexts/WhoAmIContext'
import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import getWhoAmI from './getWhoami'
import { Header } from './components/Header'
const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Authenticated',
  description: 'Generated by create next app',
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  const { tenants, user } = await getWhoAmI()

  return (
    <WhoAmIContext user={user} tenants={tenants}>
      <Header />
      {children}
    </WhoAmIContext>
  )
}
