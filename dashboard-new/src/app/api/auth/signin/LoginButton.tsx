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
      className="bg-blue-500 py-4 text-white font-semibold rounded-md py-2 px-4 lg:w-96 md:w-96 w-full"
    >
      Login with {name}
    </button>
  )
}
