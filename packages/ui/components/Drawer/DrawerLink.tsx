interface Props {
    name?:string
}
export const DrawerLink = ({name}:Props) => {
    return <div className="drawer-link">
        <img src={`/link.svg`} alt="link" style={{ width:"20px", height:"10px"}} />
        {/* <Image src={linkSvg} alt={"link"} width={20} height={10} /> */}
							<p className="drawer__task__link__container__clickable__text">
							{/* {data?.name?.split("-").slice(1, -1).join("-") || ""} */}
                            {name}
							</p>
    </div>
}