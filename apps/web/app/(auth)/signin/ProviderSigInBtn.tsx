"use client";

import { signIn, useSession } from "next-auth/react"
import Image from "next/image";
import { redirect } from 'next/navigation';

export const  ProviderSigInBtn = ({provider, num}:{provider:any, num:number}) =>  {

    const { data: session } = useSession()
    if(session) redirect('/');

    return (
        <>
          <button type="button" className="login-button" onClick={() => signIn(provider.id)}>
            <div>
              <Image src="/key.svg" width={22} height={12} alt="login" />
            </div>
            {num > 1 ? `Log in using ${provider.name}` : `Log in using SSO`}
          </button>
        </>
    )
}

