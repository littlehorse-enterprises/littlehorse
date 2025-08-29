'use client'
import { ClientSafeProvider, signIn } from 'next-auth/react'
import { useSearchParams } from 'next/navigation'
import { FC } from 'react'

export const LoginButton: FC<Pick<ClientSafeProvider, 'id' | 'name'>> = ({ id, name }) => {
  const searchParams = useSearchParams()
  const callbackUrl = searchParams.get('callbackUrl') || '/'
  return (
    <button
      onClick={() => signIn(id, { callbackUrl })}
      className="w-full rounded-md bg-blue-500 px-4 py-4 font-semibold text-white md:w-96 lg:w-96"
    >
      Login with {name}
    </button>
  )
}
