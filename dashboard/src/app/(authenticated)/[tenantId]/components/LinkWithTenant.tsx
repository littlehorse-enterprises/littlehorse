'use client'
import { withTenant } from '@/app/routes'
import { cn } from '@/components/utils'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import NextLink from 'next/link'
import { ComponentProps } from 'react'

const buildTenantHref = (tenantId: string, href: string) => {
  if (href === '/') {
    return `/${tenantId}`
  }
  if (href.startsWith('?')) {
    return `/${tenantId}${href}`
  }
  return withTenant(tenantId, href)
}

type LinkWithTenantProps = Omit<ComponentProps<typeof NextLink>, 'href'> & {
  href: string
  linkStyle?: boolean
}

const LinkWithTenant = ({ linkStyle, href, ...props }: LinkWithTenantProps) => {
  const { tenantId } = useWhoAmI()

  return (
    <NextLink
      {...props}
      href={buildTenantHref(tenantId, href)}
      className={cn(props.className, {
        '[&>*:first-child]:text-blue-500 [&>*:first-child]:hover:underline': linkStyle,
      })}
    />
  )
}

export default LinkWithTenant
