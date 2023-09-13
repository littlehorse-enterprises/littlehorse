interface Props {
    name:string
    onClick: (thread:string) => void
}
export const DrawerThreadSpecLink = ({name, onClick}:Props) => {
    return <div className="drawer-link" onClick={() => onClick(name)}>
        <img src={`/polyline.svg`} alt="link" />
        <p className="drawer__task__link__container__clickable__text">{name}</p>
</div>
}