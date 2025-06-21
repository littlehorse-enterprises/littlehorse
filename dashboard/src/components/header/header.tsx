'use client'

import TenantHeader from './TenantHeader'
import Logo from '../ui/logo'
import Link from 'next/link'
import { useParams } from 'next/navigation'

export default function Header() {
  const tenantId = useParams().tenantId as string
  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 px-4 bg-black">
      <div className="flex items-center space-x-8">
        <Link href={`/${tenantId}`} className="text-white hover:text-[#3b81f5]">
          <Logo />
        </Link>

        <nav className="flex space-x-8">

        </nav>
      </div>

      <TenantHeader />
    </header>
  )
}
