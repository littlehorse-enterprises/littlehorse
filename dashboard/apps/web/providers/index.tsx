'use client'

import { SessionProvider, useSession } from 'next-auth/react'
import { Loader } from 'ui'
import { LoginPage } from '../app/(auth)/signin/LoginPage'
import { useEffect, useState } from 'react'
import type { SessionWithJWTExpireTime } from '../pages/api/auth/[...nextauth]'

interface CheckSessionProps {
    children?: React.ReactNode;
}

function CheckSession({ children }: CheckSessionProps) {
    const { data: session } = useSession()
    const [ sessionIsActive, setSessionIsActive ] = useState(false)

    useEffect(() => {
        if (__AUTHENTICATION_ENABLED__) {
            if (!session) {
                setSessionIsActive(false)
            } else {
                const tokenExpireTime = (session as unknown as SessionWithJWTExpireTime).expireTime
                const tokenHasExpired = new Date() > new Date(tokenExpireTime * 1000)

                if (tokenHasExpired) {
                    setSessionIsActive(false)
                } else {
                    setSessionIsActive(true)
                }
            }
        }
    }, [ session ])


    if (__AUTHENTICATION_ENABLED__) {
        if (sessionIsActive) {
            return <>
                {children}
            </>
        }
        if (!sessionIsActive) {
            return <LoginPage />
        }
        return <Loader />
    }

    return <>
        {children}
    </>
}
export function Providers({ children }: CheckSessionProps) {

    return <SessionProvider>
        <CheckSession>
            {children}
        </CheckSession>
    </SessionProvider>
}
