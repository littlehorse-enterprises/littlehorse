'use client'

import { getServerVersion } from '@/app/actions/getServerVersion'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import LhLogo from '@/littlehorse.svg'
import { FC } from 'react'
import useSWR from 'swr'

import LinkWithTenant from './LinkWithTenant'
import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'

export const Header: FC = () => {
  const { tenantId } = useWhoAmI()
  const { data: serverVersion, error: serverVersionError } = useSWR(
    tenantId ? (['serverVersion', tenantId] as const) : null,
    async ([_cacheKey, tenantIdForRequest]) => getServerVersion(tenantIdForRequest)
  )

  const serverLabel =
    serverVersionError !== undefined ? '—' : serverVersion === undefined ? '…' : serverVersion

  return (
    <nav className="mb-4 border-gray-200 bg-black px-8 dark:bg-gray-900">
      <div className="mx-auto flex flex-wrap items-center justify-between py-4">
        <LinkWithTenant href="/" className="flex items-center space-x-1 rtl:space-x-reverse">
          <LhLogo className="h-8 fill-white" />
          <div className="hidden flex-col gap-0 space-y-[-10px] text-xl font-bold text-white md:flex">
            <span>LITTLE</span>
            <span>HORSE</span>
          </div>
        </LinkWithTenant>
        <div className="flex items-center gap-4">
          <span
            className="whitespace-nowrap text-sm text-gray-400"
            title="LittleHorse server (GetServerVersion) · dashboard build"
          >
            <span className="text-gray-500">Server</span> {serverLabel}
            <span className="ml-2 text-xs opacity-70">
              · UI {process.env.NEXT_PUBLIC_VERSION ?? 'v0.0.0-dev'}
            </span>
          </span>
          <TenantSelector />
          <Principal />
        </div>
      </div>
    </nav>
  )
}
