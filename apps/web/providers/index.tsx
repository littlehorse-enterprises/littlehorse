"use client";

import { SessionProvider, useSession } from "next-auth/react";

type Props = {
  children?: React.ReactNode;
};

const CheckSession = ({ children }: Props) => {


  const { data: session, status } = useSession()
    return <>
    {children}
    </>

}
export const Providers = ({ children }: Props) => {

  return <SessionProvider>
    <CheckSession>
      {children}
    </CheckSession>
  </SessionProvider>;
};
