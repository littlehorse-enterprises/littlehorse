"use client";

import { useState, useRef } from "react";
import {useOutsideClick} from "../../utils"

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

    const handleOutsideClick = () => {
        setOpen(false)
    }

    const ancestorRef = useRef<HTMLDivElement>(null);
    const ref = useOutsideClick(handleOutsideClick, ancestorRef);

    return <div className="perpage-canvas" ref={ancestorRef}>
        <img src={props.icon} alt={props.icon}></img>
        <input onClick={() => setOpen(prev => !prev)} readOnly className={`input ${!!props.icon ? 'icon' : undefined}`} value={props.value} onChange={() => {}} />
        {open ? <div className="options" ref={ref}>
            {props.values.map( v => <div key={v} onClick={() => setValue(v)} >{v}</div>)}
        </div> : undefined}
    </div>
}