import { FC, ReactNode } from 'react'

export const SpecEmpty: FC<{ children?: ReactNode }> = ({ children = 'None' }) => (
  <p className="text-sm text-muted-foreground">{children}</p>
)

export const SpecSectionTitle: FC<{ children: ReactNode }> = ({ children }) => (
  <h3 className="mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">{children}</h3>
)
