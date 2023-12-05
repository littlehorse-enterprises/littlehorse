import React from 'react'

export type Props = React.HTMLAttributes<HTMLHeadElement>

function H2({ children, ...props }: Props) {
  return <h2 {...props}>{children}</h2>
}

export default H2
