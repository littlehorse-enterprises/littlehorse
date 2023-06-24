import React from 'react'

export interface Props extends React.HTMLAttributes<HTMLHeadElement> {}

const H2 = ({ children, ...props }: Props) => {
	return <h2 {...props}>{children}</h2>
}

export default H2
