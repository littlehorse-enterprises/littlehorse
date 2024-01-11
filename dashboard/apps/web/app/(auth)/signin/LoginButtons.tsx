'use client'

import type { ClientSafeProvider } from 'next-auth/react'
import { getProviders } from 'next-auth/react'
import { useEffect, useState } from 'react'
import { ProviderSigInBtn } from './ProviderSigInBtn'

export function LoginButtons() {
    const [ vProviders, setVProviders ] = useState<ClientSafeProvider[]>([])

    useEffect(() => {
        getProviders().then(providers => {
            setVProviders(Object.values(providers || []))
        }, rejectedReason => {
            console.error('Not able to get Auth providers', rejectedReason)
        })
    }, [])

    return (
        <>
            {vProviders.map((provider) => <ProviderSigInBtn key={provider.id} num={vProviders.length} provider={provider} />)}
        </>
    )
}
