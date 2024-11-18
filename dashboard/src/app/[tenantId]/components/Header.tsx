'use client'

import { FC } from 'react'
import LhLogo from '@/littlehorse.svg'

import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'
import { useParams } from 'next/navigation'

export const Header: FC = () => {
  const { tenantId } = useParams()

  return (
    <nav className="mb-4 border-gray-200 bg-black px-8 dark:bg-gray-900">
      <div className="mx-auto flex max-w-screen-xl flex-wrap items-center justify-between py-4">
        <a href={`/${tenantId}`} className="flex items-center space-x-1 rtl:space-x-reverse">
          <LhLogo className="h-8 fill-white" />
          <div className="hidden flex-col gap-0 space-y-[-10px] text-xl font-bold text-white md:flex">
            <span>LITTLE</span>
            <span>HORSE</span>
          </div>
        </a>
        <div className="flex items-center">
          <TenantSelector />
          <Principal />
        </div>
      </div>
    </nav>
  )
}
