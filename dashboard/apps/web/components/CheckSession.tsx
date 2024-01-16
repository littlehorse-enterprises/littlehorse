'use client'

import { useSession } from 'next-auth/react'
import { Loader } from 'ui'
import { LoginPage } from '../app/(auth)/signin/LoginPage'
import { useEffect, useState } from 'react'
import type { SessionWithJWTExpireTime } from '../pages/api/auth/[...nextauth]'
import { useFeatureToggle } from '../contexts/FeatureToggleContext'


interface CheckSessionProps {
    children?: React.ReactNode;
}

export function CheckSession({ children }: CheckSessionProps) {
    const { data: session } = useSession()
    const [ sessionIsActive, setSessionIsActive ] = useState(false)
    const isAuthenticationEnabled = useFeatureToggle('isAuthenticationEnabled')

    useEffect(() => {
        if (isAuthenticationEnabled) {
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


    if (isAuthenticationEnabled) {
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
