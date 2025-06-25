'use client'

import { createContext, useContext, type ReactNode } from 'react'

interface SectionContextValue {
    isNested: boolean
}

interface SectionProviderProps {
    children: ReactNode
    isNested: boolean
}

const SectionContext = createContext<SectionContextValue | undefined>(undefined)

export function SectionProvider({ children, isNested }: SectionProviderProps) {
    return <SectionContext.Provider value={{ isNested }}>{children}</SectionContext.Provider>
}

export function useSectionContext() {
    const context = useContext(SectionContext)
    if (context === undefined) {
        return { isNested: false }
    }
    return context
} 