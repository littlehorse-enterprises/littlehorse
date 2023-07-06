"use client";

/* eslint-disable @next/next/no-img-element */
import Image from "next/image";
import { signOut, useSession } from "next-auth/react";
import { useState, useRef } from "react";
import { useOutsideClick } from "ui";

const Avatar = ({session}) => {
    return (
      <div className="avatar">
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
    )
}

export const LoginDropdown = () => {

    // ref used to locate the ancestor Ref so the handler doesn't reopen the log out option
    const ancestorOutsideClickRef = useRef<HTMLDivElement>(null);
    const outsideClickRef = useOutsideClick(() => setActive(false),ancestorOutsideClickRef);

    const { data: session } = useSession();
    const [active, setActive] = useState<boolean>(false);

    return (
      <>
        <div className="login-dropdown" ref={ancestorOutsideClickRef}>
            <div className={`login-dropdown__btn ${active && 'active'}`} onClick={() => setActive(prev => !prev)}>
                <Avatar session={session} />
                {session?.user?.email}
                <Image src="/expand_more.svg" alt="expand" width={12} height={7} />
            </div>
            {active && <div className="login-dropdown__dd" ref={outsideClickRef}>
                <button onClick={() => signOut()}>Log out</button>
            </div>}
        </div>
      </>
    )
}
