'use client'

import { Toaster } from '@littlehorse-enterprises/ui-library/sonner'
import { SessionProvider } from 'next-auth/react'

interface ProvidersProps {
  children: React.ReactNode
}

export default function Providers({ children }: ProvidersProps) {
  return (
    <SessionProvider>
      {children}
      <Toaster />
    </SessionProvider>
  )
}
