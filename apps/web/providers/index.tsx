"use client";

import { SessionProvider, useSession } from "next-auth/react";
import { LoginPage } from "../app/(auth)/signin/LoginPage";
import { Loader } from "ui";

type Props = {
  children?: React.ReactNode;
};

const CheckSession = ({ children }: Props) => {


  const { data: session, status } = useSession()
  if (status === "authenticated") {
    return <>
    {children}
    </>
  }
  if (status === "unauthenticated") {
    return <LoginPage />
  }
  return <Loader />

}
export const Providers = ({ children }: Props) => {

  return <SessionProvider>
    <CheckSession>
      {children}
    </CheckSession>
  </SessionProvider>;
};
