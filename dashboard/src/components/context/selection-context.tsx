'use client'

import { createContext, useContext, useState, type ReactNode } from 'react'

interface NodeSelectionContextValue {
  selectedId: string | null
  setSelectedId: (id: string | null) => void
}

interface NodeSelectionProviderProps {
  children: ReactNode
}

const NodeSelectionContext = createContext<NodeSelectionContextValue | undefined>(undefined)

export function NodeSelectionProvider({ children }: NodeSelectionProviderProps) {
  const [selectedId, setSelectedId] = useState<string | null>(null)

  return <NodeSelectionContext.Provider value={{ selectedId, setSelectedId }}>{children}</NodeSelectionContext.Provider>
}

export function useNodeSelection() {
  const context = useContext(NodeSelectionContext)
  if (context === undefined) {
    throw new Error('useNodeSelection must be used within a NodeSelectionProvider')
  }
  return context
}
