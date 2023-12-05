'use client'
interface DrawerHeaderProps {
  title?:string
  image?:string
  name?:string
}
export function DrawerHeader({ name, image, title }:DrawerHeaderProps) {
  return <div className='component-header'>
    <img alt={image} src={`/${image}.svg`} />
    <div>
      <p>{title}</p>
      <p className='component-header__subheader'>{name?.split('-').slice(0,-1).join('-')}</p>
    </div>
  </div>
}
