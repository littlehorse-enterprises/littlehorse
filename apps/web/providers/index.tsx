'use client'

import { SessionProvider, useSession } from 'next-auth/react'
import { Loader } from 'ui'
import { LoginPage } from '../app/(auth)/signin/LoginPage'

interface Props {
    children?: React.ReactNode;
}

function CheckSession({ children }: Props) {


    const { data: session, status } = useSession()
    if (status === 'authenticated') {
        return <>
            {children}
        </>
    }
    if (status === 'unauthenticated') {
        return <LoginPage />
    }
    return <Loader />

}
export function Providers({ children }: Props) {

    return <SessionProvider>
        <CheckSession>
            {children}
        </CheckSession>
    </SessionProvider>
}
