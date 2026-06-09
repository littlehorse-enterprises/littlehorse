import { cn } from '@/components/utils'
import { FC, ReactNode } from 'react'

export const SpecTag: FC<{ children: ReactNode; className?: string; mono?: boolean }> = ({
  children,
  className,
  mono,
}) => (
  <span
    className={cn(
      'rounded-full bg-muted px-2 py-0.5 text-xs text-muted-foreground',
      mono && 'font-mono text-foreground',
      className
    )}
  >
    {children}
  </span>
)

export const SpecEmpty: FC<{ children?: ReactNode }> = ({ children = 'None' }) => (
  <p className="text-sm text-muted-foreground">{children}</p>
)

export const SpecSectionTitle: FC<{ children: ReactNode }> = ({ children }) => (
  <h3 className="mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground">{children}</h3>
)
