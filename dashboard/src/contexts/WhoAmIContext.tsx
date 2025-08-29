'use client'

import { DefaultSession } from 'next-auth'
import { FC, PropsWithChildren, createContext, useContext } from 'react'

type ContextProps = {
  user: DefaultSession['user']
  tenantId: string
  tenants: string[]
}

const Context = createContext<ContextProps>({
  user: {},
  tenantId: '',
  tenants: [],
})

type WhoAmIContextProps = ContextProps

export const WhoAmIContext: FC<PropsWithChildren<WhoAmIContextProps>> = ({ children, user, tenants, tenantId }) => {
  return <Context.Provider value={{ user, tenants, tenantId: tenantId || tenants[0] }}>{children}</Context.Provider>
}

export const useWhoAmI = () => {
  const context = useContext(Context)

  return context
}
