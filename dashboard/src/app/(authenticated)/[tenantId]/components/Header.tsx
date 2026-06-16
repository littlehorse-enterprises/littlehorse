'use client'

import { getServerVersion } from '@/app/actions/getServerVersion'
import { routes } from '@/app/routes'
import { useWhoAmI } from '@/contexts/WhoAmIContext'
import LhLogo from '@/littlehorse.svg'
import { FC } from 'react'
import useSWR from 'swr'

import { JumpToWfRun } from './JumpToWfRun'
import LinkWithTenant from './LinkWithTenant'
import { Principal } from './Principal'
import { TenantSelector } from './TenantSelector'

export const Header: FC = () => {
  const { tenantId } = useWhoAmI()
  const { data: serverVersion, error: serverVersionError } = useSWR(
    tenantId ? (['serverVersion', tenantId] as const) : null,
    async ([_cacheKey, tenantIdForRequest]) => getServerVersion(tenantIdForRequest)
  )

  const serverLabel = serverVersionError !== undefined ? '—' : serverVersion === undefined ? '…' : serverVersion

  return (
    <nav className="mb-4 border-gray-200 bg-black px-8 dark:bg-gray-900">
      <div className="mx-auto py-4">
        <div className="flex items-center justify-between gap-4 md:hidden">
          <LinkWithTenant href={routes.appRoot()} className="flex items-center space-x-1 rtl:space-x-reverse">
            <LhLogo className="h-8 fill-white" />
          </LinkWithTenant>
          <div className="flex items-center gap-4">
            <TenantSelector />
            <Principal />
          </div>
        </div>

        <div className="mt-3 flex flex-col gap-3 md:mt-0 md:flex-row md:items-center md:justify-between md:gap-6">
          <div className="flex min-w-0 items-center gap-4 md:gap-12">
            <LinkWithTenant
              href={routes.appRoot()}
              className="hidden shrink-0 items-center space-x-1 md:flex rtl:space-x-reverse"
            >
              <LhLogo className="h-8 fill-white" />
              <div className="hidden flex-col gap-0 space-y-[-10px] text-xl font-bold text-white lg:flex">
                <span>LITTLE</span>
                <span>HORSE</span>
              </div>
            </LinkWithTenant>

            <div className="w-full md:w-72">
              <JumpToWfRun variant="global" />
            </div>
          </div>

          <div className="hidden shrink-0 items-center gap-4 md:flex">
            <span
              className="hidden whitespace-nowrap text-sm text-gray-400 lg:inline"
              title="LittleHorse server (GetServerVersion) · dashboard build"
            >
              <span className="text-gray-500">Server</span> {serverLabel}
              <span className="ml-2 text-xs opacity-70">· UI {process.env.NEXT_PUBLIC_VERSION ?? 'v0.0.0-dev'}</span>
            </span>
            <TenantSelector />
            <Principal />
          </div>
        </div>
      </div>
    </nav>
  )
}
