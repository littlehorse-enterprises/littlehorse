'use client'

import { Badge } from '@littlehorse-enterprises/ui-library/badge'
import LinkWithTenant from '../link-with-tenant'
import Logo from '../ui/logo'
import TenantHeader from './TenantHeader'

export default function Header() {
  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 bg-black px-4">
      <div className="flex items-center space-x-8">
        <LinkWithTenant href="/" className="mr-2">
          <Logo />
        </LinkWithTenant>

        <Badge variant="secondary" className="text-xs opacity-60">
          {process.env.NEXT_PUBLIC_VERSION}
        </Badge>
      </div>

      <div className="flex items-center gap-4">
        <TenantHeader />
      </div>
    </header>
  )
}
