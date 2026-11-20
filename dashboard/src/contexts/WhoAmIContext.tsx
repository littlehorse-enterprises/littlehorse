'use client'

import { DefaultSession } from 'next-auth'
import { useParams } from 'next/navigation'
import { FC, PropsWithChildren, createContext, useContext, useMemo } from 'react'

export type ContextProps = {
  user: DefaultSession['user']
  tenantId: string
  tenants: string[]
  tenantNames: Record<string, string>
}

const Context = createContext<ContextProps>({
  user: {},
  tenantId: '',
  tenants: [],
  tenantNames: {},
})

type WhoAmIContextProps = ContextProps

export const WhoAmIContext: FC<PropsWithChildren<WhoAmIContextProps>> = ({
  children,
  user,
  tenants,
  tenantNames,
  tenantId,
}) => {
  const params = useParams()
  const currentTenantId = (params.tenantId as string) || tenantId || 'default'
  const value = useMemo(
    () => ({ user, tenants, tenantNames, tenantId: currentTenantId }),
    [user, tenants, tenantNames, currentTenantId]
  )
  return <Context.Provider value={value}>{children}</Context.Provider>
}

export const useWhoAmI = () => {
  const context = useContext(Context)

  return context
}
