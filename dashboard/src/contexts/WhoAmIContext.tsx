'use client'

import { DefaultSession } from 'next-auth'
import { useParams } from 'next/navigation'
import { FC, PropsWithChildren, createContext, useContext, useMemo } from 'react'

export type ContextProps = {
  user: DefaultSession['user']
  tenantId: string
  tenants: string[]
}

const Context = createContext<ContextProps>({
  user: {},
  tenantId: '',
  tenants: [],
})

type WhoAmIContextProps = Omit<ContextProps, 'tenantId'> & { tenantId?: string }

export const WhoAmIContext: FC<PropsWithChildren<WhoAmIContextProps>> = ({ children, user, tenants, tenantId: initialTenantId }) => {
  const params = useParams()
  const currentTenantId = (params?.tenantId as string) || initialTenantId || 'default'

  const value = useMemo(
    () => ({ user, tenants, tenantId: currentTenantId }),
    [user, tenants, currentTenantId]
  )
  return <Context.Provider value={value}>{children}</Context.Provider>
}

export const useWhoAmI = () => {
  const context = useContext(Context)

  return context
}
