import { SessionProvider } from '../components/SessionProvider'
import FeatureToggleProvider from '../contexts/FeatureToggleContext'
import React from 'react'
import { CheckSession } from '../components/CheckSession'

interface CheckSessionProps {
    children?: React.ReactNode;
}
export function Providers({ children }: CheckSessionProps) {
    const toggles = {
        isAuthenticationEnabled: process.env.AUTHENTICATION_ENABLED,
    }

    return (
        <FeatureToggleProvider value={toggles}>
            <SessionProvider>
                <CheckSession>
                    {children}
                </CheckSession>
            </SessionProvider>
        </FeatureToggleProvider>
    )
}
