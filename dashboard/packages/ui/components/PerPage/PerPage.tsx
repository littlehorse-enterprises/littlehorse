'use client'

import { useState, useRef } from 'react'
import { useOutsideClick } from '../../utils'

export interface PerPageProps {
  icon:string
  values:number[]
  value:number
  onChange: (v:number) => void
}

export function PerPage(props: PerPageProps) {
  const [ open, setOpen ] = useState(false)

  const setValue = (v:number) => {
    props.onChange(v)
    setOpen(false)
  }

  const handleOutsideClick = () => {
    setOpen(false)
  }

  const ancestorRef = useRef<HTMLDivElement>(null)
  const ref = useOutsideClick(handleOutsideClick, ancestorRef)

  return <div className="perpage-canvas" ref={ancestorRef}>
    <img alt={props.icon} src={props.icon} />
    <input className={`input ${props.icon ? 'icon' : undefined}`} onClick={() => { setOpen(prev => !prev) }} readOnly value={props.value} />
    {open ? <div className="options" ref={ref}>
      {props.values.map( v => <div key={v} onClick={() => { setValue(v) }} >{v}</div>)}
    </div> : undefined}
  </div>
}
