"use client";

import { signOut, useSession } from "next-auth/react";
import { useState } from "react";

const Avatar = ({session}:any) => {
    return <div className="avatar">
    {session?.user?.image && (
      <img
        src={session?.user?.image}
        alt={session?.user?.email || ""}
      />
    )}
    {!session?.user?.image && session?.user?.name && (
      <span>
        {session.user.name
          .split(" ", 2)
          .map((t) => t[0])
          .join("")}
      </span>
    )}
  </div>
}

export const LoginDropdown = () => {
    const { data: session } = useSession();
    const [active, setActive] = useState(false);
    return <>
        <div className="login-dropdown">
            <div className={`login-dropdown__btn ${active && 'active'}`} onClick={() => setActive(prev => !prev)}>
                <Avatar session={session} />
                {session?.user?.email}
                <img src="/expand_more.svg" alt="expand" />
            </div>
            {active && <div className="login-dropdown__dd">
                <button onClick={() => signOut()}>Log out</button>
            </div>}
        </div>
  
    </>
}