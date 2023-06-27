"use client";

import { useState } from "react";

export interface Props {
 icon:string
 values:number[]
 value:number 
 onChange: (v:number) => void
}

export const PerPage = (props: Props) => {
    const [open, setOpen] = useState(false)

    const setValue = (v:number) => {
        props.onChange(v)
        setOpen(false)
    }

    return <div className="perpage-canvas" >
        <img src={props.icon} alt={props.icon}></img>
        <input onClick={() => setOpen(prev => !prev)} readOnly className={`input ${!!props.icon ? 'icon' : undefined}`} value={props.value} onChange={() => {}} />
        {open ? <div className="options">
            {props.values.map( v => <div key={v} onClick={() => setValue(v)} >{v}</div>)}
        </div> : undefined}
    </div>
}