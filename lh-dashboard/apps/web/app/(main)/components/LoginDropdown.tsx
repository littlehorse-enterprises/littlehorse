'use client'

 
import Image from 'next/image'
import { signOut, useSession } from 'next-auth/react'
import { useState, useRef } from 'react'
import { useOutsideClick } from 'ui'

function Avatar({ session }) {
    return (
        <div className="avatar">
            {session?.user?.image ? <img
                alt={session?.user?.email || ''}
                src={session?.user?.image}
            /> : null}
            {!session?.user?.image && session?.user?.name ? <span>
                {session.user.name
                    .split(' ', 2)
                    .map((t: string) => t[0])
                    .join('')}
            </span> : null}
        </div>
    )
}

export function LoginDropdown() {
    // ref used to locate the ancestor Ref so the handler doesn't reopen the log out option
    const ancestorOutsideClickRef = useRef<HTMLDivElement>(null)
    const outsideClickRef = useOutsideClick(() => { setActive(false) },ancestorOutsideClickRef)

    const { data: session } = useSession()
    const [ active, setActive ] = useState<boolean>(false)

    if (__AUTHENTICATION_ENABLED__) {
        return (
            <div className="login-dropdown" ref={ancestorOutsideClickRef}>
                <div className={`login-dropdown__btn ${active && 'active'}`} onClick={() => { setActive(prev => !prev) }}>
                    <Avatar session={session} />
                    {session?.user?.email}
                    <Image alt="expand" height={7} src="/expand_more.svg" width={12} />
                </div>
                {active ? <div className="login-dropdown__dd" ref={outsideClickRef}>
                    <button onClick={() => signOut()}>Log out</button>
                </div> : null}
            </div>
        )
    } 

    return <div />
}
