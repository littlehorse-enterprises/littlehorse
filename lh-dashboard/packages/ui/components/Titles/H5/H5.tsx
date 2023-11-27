import React from 'react'

export type Props = React.HTMLAttributes<HTMLHeadElement>

function H5({ children, ...props }: Props) {
  return <h5 {...props}>{children}</h5>
}

export default H5
