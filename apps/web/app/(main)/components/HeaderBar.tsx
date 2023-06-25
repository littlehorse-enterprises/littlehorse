import Image from "next/image";
import { HeaderBar as Header } from "ui";
import { LoginDropdown } from "./LoginDropdown";
import Link from "next/link";

export const HeaderBar = () => {
    return <Header>
        <Link href={'/'}><Image src="/LH Logo.svg" width={163} height={24} alt="LittleHorse" /></Link>
        <LoginDropdown />
    </Header>
}