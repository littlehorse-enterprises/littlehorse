import Image from 'next/image'
import { HeaderBar as Header } from 'ui'
import Link from 'next/link'
import { LoginDropdown } from './LoginDropdown'

export function HeaderBar() {
    return <Header>
        <Link href="/"><Image alt="LittleHorse" height={24} src="/LH Logo.svg" width={163} /></Link>
        <LoginDropdown />
    </Header>
}