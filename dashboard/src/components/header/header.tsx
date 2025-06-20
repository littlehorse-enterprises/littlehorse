'use client'

import Image from 'next/image'
import Link from 'next/link'
import { useParams } from 'next/navigation'
import TenantHeader from './TenantHeader'

export default function Header() {
  const tenantId = useParams().tenantId as string

  return (
    <header className="flex h-16 items-center justify-between border-b border-gray-200 px-4">
      <div className="flex items-center space-x-8">
        <div className="flex items-center">
          <Image
            src="/placeholder.svg?height=24&width=24"
            alt="LittleHorse Logo"
            width={24}
            height={24}
            className="mr-2"
          />
          <span className="text-lg font-bold">LittleHorse</span>
        </div>

        <nav className="flex space-x-8">
          <Link href={`/${tenantId}`} className="text-[#656565] hover:text-[#3b81f5]">
            Dashboard
          </Link>
        </nav>
      </div>

      <TenantHeader />
    </header>
  )
}
