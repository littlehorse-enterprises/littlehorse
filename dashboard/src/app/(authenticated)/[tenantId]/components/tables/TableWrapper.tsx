import { FC, ReactNode } from 'react'

type TableWrapperProps = {
  children: ReactNode
}

export const TableWrapper: FC<TableWrapperProps> = ({ children }) => {
  return (
    <div className="overflow-hidden rounded-lg border">
      <div className="max-h-[600px] overflow-auto">{children}</div>
    </div>
  )
}
