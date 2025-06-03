"use client"

import { createContext, useContext, type ReactNode, useState } from "react"

interface SelectionContextValue {
    selectedId: string | null
    setSelectedId: (id: string | null) => void
}

interface SelectionProviderProps {
    children: ReactNode
}

const SelectionContext = createContext<SelectionContextValue | undefined>(undefined)

export function SelectionProvider({ children }: SelectionProviderProps) {
    const [selectedId, setSelectedId] = useState<string | null>(null)

    return (
        <SelectionContext.Provider value={{ selectedId, setSelectedId }}>
            {children}
        </SelectionContext.Provider>
    )
}

export function useSelection() {
    const context = useContext(SelectionContext)
    if (context === undefined) {
        throw new Error("useSelection must be used within a SelectionProvider")
    }
    return context
} 