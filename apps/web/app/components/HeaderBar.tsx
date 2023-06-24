import Image from "next/image";
import { HeaderBar as Header } from "ui";
import { LoginDropdown } from "./LoginDropdown";

export const HeaderBar = () => {
    return <Header>
        <Image src="./LH Logo.svg" width={163} height={24} alt="LittleHorse" /> 
        <LoginDropdown />
    </Header>
}