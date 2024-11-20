'use client'
import { LinkProps } from 'next/link'
import NextLink from 'next/link'
import { useParams } from 'next/navigation'
import { ComponentProps } from 'react'

const LinkWithTenant = ({ ...props }: ComponentProps<typeof NextLink>) => {
  const { tenantId } = useParams()
  return <NextLink {...props} href={`/${tenantId}${props.href}`} />
}

export default LinkWithTenant
