"use client";
interface Props {
    title?:string
    image?:string
    name?:string
}
export const DrawerHeader = ({name, image, title}:Props) => {
    return <div className='component-header'>
    <img src={`/${image}.svg`} alt={image} />
    <div>
        <p>{title}</p>
        <p className='component-header__subheader'>{name && name.split('-').slice(0,-1).join('-')}</p>
    </div>
</div>
}