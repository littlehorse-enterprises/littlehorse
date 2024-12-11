'use client'
import { cn } from '@/components/utils'
import NextLink from 'next/link'
import { useParams } from 'next/navigation'
import { ComponentProps } from 'react'

const LinkWithTenant = ({ linkStyle, ...props }: ComponentProps<typeof NextLink> & { linkStyle?: boolean }) => {
  const { tenantId } = useParams()
  return (
    <NextLink
      {...props}
      href={`/${tenantId}${props.href}`}
      className={cn(props.className, { 'text-blue-500 underline': linkStyle })}
    />
  )
}

export default LinkWithTenant
