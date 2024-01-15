'use client'

import { SessionProvider as NextAuthSessionProvider } from 'next-auth/react'
import { FC, PropsWithChildren } from 'react'

export const SessionProvider: FC<PropsWithChildren> = ({ children }) => (
    <NextAuthSessionProvider>
        { children }
    </NextAuthSessionProvider>
)
