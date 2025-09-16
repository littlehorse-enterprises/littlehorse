'use client'

import LhLogo from '@/littlehorse.svg'
import { FC } from 'react'

import LinkWithTenant from './LinkWithTenant'
import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'

export const Header: FC = () => {
  return (
    <nav className="mb-4 border-gray-200 bg-black px-8 dark:bg-gray-900">
      <div className="mx-auto flex max-w-screen-xl flex-wrap items-center justify-between py-4">
        <LinkWithTenant href="/" className="flex items-center space-x-1 rtl:space-x-reverse">
          <LhLogo className="h-8 fill-white" />
          <div className="hidden flex-col gap-0 space-y-[-10px] text-xl font-bold text-white md:flex">
            <span>LITTLE</span>
            <span>HORSE</span>
          </div>
        </LinkWithTenant>        
        <div className="flex items-center">
          <LinkWithTenant 
            href="/workflowBuilder/new" 
            className="mr-4 rounded bg-blue-600 px-4 py-2 text-sm text-white hover:bg-blue-700"
          >
            Builder
          </LinkWithTenant>
          <TenantSelector />
          <Principal />
        </div>
      </div>
    </nav>
  )
}
