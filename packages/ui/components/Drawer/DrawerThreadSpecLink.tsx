interface Props {
    name:string
    onClick: (thread:string) => void
    label: string | undefined
}
export function DrawerThreadSpecLink({ name, onClick, label }:Props) {
  return <div className="drawer-link" onClick={() => { onClick(name) }}>
    <img alt="link" src="/polyline.svg" />
    <p className="drawer__task__link__container__clickable__text">{label}</p>
  </div>
}