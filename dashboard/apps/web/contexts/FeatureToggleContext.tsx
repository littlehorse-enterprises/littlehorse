'use client'
import { createContext, useContext } from 'react'

type FeatureToggle = Record<string, string>

const FeatureToggleContext = createContext<FeatureToggle>({ 'isAuthenticationEnabled': 'false' })

export default function FeatureToggleProvider ({ children, value }) {
    return (
        <FeatureToggleContext.Provider value={value}>
            {children}
        </FeatureToggleContext.Provider>
    )
}

export const useFeatureToggle = (name: string): boolean => {
    const context = useContext(FeatureToggleContext)

    return context[name] === 'true'
}
