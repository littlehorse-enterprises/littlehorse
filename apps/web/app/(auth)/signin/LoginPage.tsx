import Image from 'next/image'
import { Suspense } from 'react'
import { Loader } from 'ui'
import { LoginButtons } from './LoginButtons'


export function LoginPage() {

  return <div className="login">
    <div className="login_horse" />
    <div className="login_form">
      <Image alt="logo-lh" height={80} src="/logo-lh.svg" width={218}/>
      <form>
        <div className="title">
            Welcome to your
          <span className="display-block">Little Horse</span>
          <span className="mb-40 display-block color-primary">dashboard</span>
        </div>
        <Suspense fallback={<Loader />}>
          <LoginButtons />
        </Suspense>
          
      </form>
      <div className="legals">Copyright © 2023 LittleHorse Enterprises LLC. </div>
    </div>

  </div>

  // <div className="flex min-h-full">
  //     <div style={{
  //         background:`url("/handsome-horse.svg")`,
  //         backgroundPosition: "center center",
  //         backgroundSize: "cover",
  //         flex:1,
            
  //     }}></div>
  //     <div className="flex flex-col justify-between flex-1 gap-4 py-4 pt-4 pr-4 mt-4 mr-4 justify-self-center gap login">
  //         <div className="grid justify-items-end">
  //             <img className="logo" src={'/lh-logo-logIn.svg'} />
  //         </div>
  //         <div className="flex grid flex-col gap-4 justify-items-center">
  //             <p className="welcome-message md:w-6/12">Welcome to your Little Horse <span className="text-[#7F7AFF]">dashboard</span></p>
  //             <Suspense fallback={'Loading...'}>
  //                 {/* @ts-expect-error Server Component */}
  //                 <LoginButtons></LoginButtons>
  //             </Suspense>
  //         </div>
  //         <div className="login footer">Copyright © 2023 LittleHorse Enterprises LLC. </div>
  //     </div>
  // </div>
}