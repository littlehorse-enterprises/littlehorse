'use client'

import { DefaultSession } from 'next-auth'
import { FC, PropsWithChildren, createContext, useCallback, useContext, useState } from 'react'

type ContextProps = {
  user: DefaultSession['user']
  tenantId: string
  tenants: string[]
  setTenantId: (tenantId: string) => void
}

const Context = createContext<ContextProps>({
  user: {},
  tenantId: '',
  tenants: [],
  setTenantId: () => {},
})

type WhoAmIContextProps = { user: DefaultSession['user']; tenants: string[] }

export const WhoAmIContext: FC<PropsWithChildren<WhoAmIContextProps>> = ({ children, user, tenants }) => {
  const [tenantId, internalSetTenantId] = useState(tenants[0])

  const setTenantId = useCallback((tenant: string) => {
    internalSetTenantId(tenant)
  }, [])

  return <Context.Provider value={{ user, tenantId, tenants, setTenantId }}>{children}</Context.Provider>
}

export const useWhoAmI = () => {
  const context = useContext(Context)

  return context
}
