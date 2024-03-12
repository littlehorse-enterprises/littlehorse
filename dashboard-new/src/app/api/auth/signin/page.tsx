import { getProviders, signIn } from 'next-auth/react'
import Image from 'next/image'
import handsomeHorse from './handsome-horse.png'
import lhLogo from './vertical-logo.svg'
import { LoginButton } from './LoginButton'
import { Metadata } from 'next'

export const metadata: Metadata = {
  title: 'Login - Littlehorse Dashboard',
}

export default async function Login() {
  const providers = await getProviders()
  const year = new Date().getFullYear()
  return (
    <div className="bg-gray-100 flex justify-center items-center h-screen">
      <div className="w-1/2 h-screen hidden lg:block">
        <Image src={handsomeHorse} alt="LittleHorse" className="object-cover w-full h-full" />
      </div>
      <div className="flex sm:20 p-8 w-full lg:w-1/2 h-full flex-col justify-between">
        <div className="flex justify-end text-blue-500">
          <Image src={lhLogo} alt="Littlehorse Logo" />
        </div>
        <div className="flex flex-grow flex-col items-center justify-center justify-items-center ">
          <h1 className="text-5xl font-bold mb-8">
            Welcome to your
            <span className="block my-3">Littlehorse</span>
            <span className="block text-blue-500">Dashboard</span>
          </h1>
          {Object.values(providers || {}).map(({ id, name }) => (
            <LoginButton key={id} id={id} name={name} />
          ))}
        </div>
        <div className="flex justify-center">Copyright &copy; {year} LittleHorse Enterprises LLC.</div>
      </div>
    </div>
  )
}
