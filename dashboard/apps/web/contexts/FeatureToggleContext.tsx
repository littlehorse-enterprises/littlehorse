'use client'
import { createContext, useContext } from 'react'

type FeatureToggle = Record<string, string>

const FeatureToggleContext = createContext<FeatureToggle>({ isAuthenticationEnabled: 'algo' })

export default function FeatureToggleProvider ({ children, value }) {
    console.error('value to the provider', value)
    return (
        <FeatureToggleContext.Provider value={value}>
            {children}
        </FeatureToggleContext.Provider>
    )
}

export const useFeatureToggle = (name: string): boolean => {
    const context = useContext(FeatureToggleContext)
    console.log('888888888888888888context', context)
    return context[name] === 'true'
}
