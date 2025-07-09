'use client'
import { cn } from '@/components/utils'
import NextLink from 'next/link'
import { useParams } from 'next/navigation'
import { ComponentProps } from 'react'

const LinkWithTenant = ({ linkStyle, href, ...props }: ComponentProps<typeof NextLink> & { linkStyle?: boolean }) => {
  const { tenantId } = useParams()

  return (
    <NextLink
      {...props}
      href={`/${tenantId}${href}`}
      className={cn(props.className, {
        '[&>*:first-child]:text-blue-500 [&>*:first-child]:hover:underline': linkStyle,
      })}
    />
  )
}

export default LinkWithTenant
