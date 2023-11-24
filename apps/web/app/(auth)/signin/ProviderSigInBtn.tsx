'use client'

import { signIn } from 'next-auth/react'
import Image from 'next/image'

export function ProviderSigInBtn({ provider, num }:{ provider:any, num:number }) {
    return (
        <button className="login-button" onClick={() => signIn(provider.id, { callbackUrl: '/' })} type="button">
            <div>
                <Image alt="login" height={12} src="/key.svg" width={22} />
            </div>
            {num > 1 ? `Log in using ${provider.name}` : `Log in using SSO`}
        </button>
    )
}

