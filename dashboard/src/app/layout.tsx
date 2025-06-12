import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { Toaster } from '@/components/ui/sonner'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'LittleHorse | Dashboard',
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
  params?: { tenantId?: string }
}>) {
  return (
    <html lang="en">
      <body className={inter.className}>
        {children}
        <Toaster position="top-center" richColors />
      </body>
    </html>
  )
}
