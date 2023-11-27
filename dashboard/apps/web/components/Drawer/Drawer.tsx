import React from 'react'

interface DrawerProps {
    title: string;
    children?: React.ReactNode;
}

export function Drawer(props: DrawerProps) {
    return (
        <div className="drawer scrollbar">
            <header className="drawer__header">
                <p>{props.title}</p>
            </header>
            {props.children}
        </div>
    )
}
