interface DrawerLinkProps {
  name?:string
}
export function DrawerLink({ name }:DrawerLinkProps) {
  return <div className="drawer-link">
    <img alt="link" src="/link.svg" style={{ width:'20px', height:'10px' }} />
    {/* <Image src={linkSvg} alt={"link"} width={20} height={10} /> */}
    <p className="drawer__task__link__container__clickable__text">
      {/* {data?.name?.split("-").slice(1, -1).join("-") || ""} */}
      {name}
    </p>
  </div>
}
