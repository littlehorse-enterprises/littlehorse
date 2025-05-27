"use client"

import { createContext, useContext, useState, type ReactNode } from "react"

interface SelectionContextType {
  selectedId: string | undefined
  setSelectedId: (id: string) => void
}

const SelectionContext = createContext<SelectionContextType | undefined>(undefined)

export function SelectionProvider({ children }: { children: ReactNode }) {
  const [selectedId, setSelectedId] = useState<string | undefined>(undefined)

  return <SelectionContext.Provider value={{ selectedId, setSelectedId }}>{children}</SelectionContext.Provider>
}

export function useSelection() {
  const context = useContext(SelectionContext)
  if (context === undefined) {
    throw new Error("useSelection must be used within a SelectionProvider")
  }
  return context
}
