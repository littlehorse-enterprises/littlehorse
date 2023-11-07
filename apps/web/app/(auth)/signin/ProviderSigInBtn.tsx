'use client'

import { signIn, useSession } from 'next-auth/react'
import Image from 'next/image'
import { redirect } from 'next/navigation'

export function ProviderSigInBtn({ provider, num }:{provider:any, num:number}) {

  const { data: session } = useSession()
  if(session) redirect('/')

  return (
    <button className="login-button" onClick={() => signIn(provider.id)} type="button">
      <div>
        <Image alt="login" height={12} src="/key.svg" width={22} />
      </div>
      {num > 1 ? `Log in using ${provider.name}` : `Log in using SSO`}
    </button>
  )
}

