import React from 'react'

export type Props = React.HTMLAttributes<HTMLHeadElement>

function H1({ children, ...props }: Props) {
  return <h1 {...props}>{children}</h1>
}

export default H1
