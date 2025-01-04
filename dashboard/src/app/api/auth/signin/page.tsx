import { Metadata } from 'next'
import Image from 'next/image'
import { LoginButton } from './LoginButton'
import handsomeHorse from './handsome-horse.png'

export const metadata: Metadata = {
  title: 'Login - Littlehorse Dashboard',
}
export const dynamic = 'force-dynamic'

export default async function Login() {
  const year = new Date().getFullYear()
  return (
    <div className="flex h-screen items-center justify-center bg-gray-100">
      <div className="hidden h-screen w-1/2 lg:block">
        <Image src={handsomeHorse} alt="LittleHorse" className="h-full w-full object-cover" />
      </div>
      <div className="sm:20 flex h-full w-full flex-col justify-between p-8 lg:w-1/2">
        <div className="flex flex-grow flex-col items-center justify-center justify-items-center ">
          <h1 className="mb-8 text-5xl font-bold">
            Welcome to your
            <span className="my-3 block">Littlehorse</span>
            <span className="block text-blue-500">Dashboard</span>
          </h1>

          <LoginButton id="keycloak" name="Keycloak" />
        </div>
        <div className="flex justify-center">Copyright &copy; {year} LittleHorse Enterprises LLC.</div>
      </div>
    </div>
  )
}
