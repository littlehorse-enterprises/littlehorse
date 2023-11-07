import React from 'react'

export interface Props extends React.HTMLAttributes<HTMLHeadElement> {}

function H3({ children, ...props }: Props) {
  return <h3 {...props}>{children}</h3>
}

export default H3
