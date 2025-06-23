'use client'

import LinkWithTenant from '../link-with-tenant'
import Logo from '../ui/logo'
import TenantHeader from './TenantHeader'

export default function Header() {
  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 px-4 bg-black">
      <div className="flex items-center space-x-8">
        <LinkWithTenant href="/" className="text-white hover:text-[#3b81f5]">
          <Logo />
        </LinkWithTenant>

        <nav className="flex space-x-8">

        </nav>
      </div>

      <TenantHeader />
    </header>
  )
}
