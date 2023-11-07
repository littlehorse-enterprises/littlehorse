'use client'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import React, { useEffect, useState } from 'react'

interface Breadcrumb {
    title: string;
    href?: string;
    active?: boolean;
}

export interface Props {
    pwd?: Breadcrumb[];
    isWfRunPage?: boolean;
    parentId?: string;
}
const kinds = []
kinds['wfspec'] = 'WfSpec'
kinds['taskdef'] = 'TaskDef'
kinds['usertaskdef'] = 'UserTaskDef'
function Breadcrumbs({ pwd, isWfRunPage, parentId }: Props) {
  const pathname = usePathname()
  const [ type, setType ] = useState('')
  const [ run, setRun ] = useState(false)
  const [ prev, setPrev ] = useState('')

  useEffect( () => {
    if(pathname) {
      const type = pathname.split('/')[1]
      setType(type)
      if([ 'wfspec', 'taskdef','usertaskdef' ].includes(type)){
        sessionStorage.setItem('prev',pathname)
                
      }else{
        setRun(true)
        setPrev(sessionStorage.getItem('prev') || '')
      }
    }
  },[ pathname ])
  return <div className="bcrumb">
    <Link href="/">Cluster Overview</Link>
    {!run && pathname ? <div> / <span className="kind">{kinds[pathname?.split('/')[1]]}:</span> {pathname?.split('/').slice(2).join(' / ')}</div> : null}
    {run && prev ? <span> / <Link href={prev}> <span className="kind">{kinds[prev?.split('/')[1]]}:</span> {prev?.split('/').slice(2).join(' / ')}</Link></span> : null}
    {run ? <div> / <span className="kind">WFRun:</span> {pathname?.split('/').slice(2).join(' / ')}</div> : null}

  </div>
}

export default Breadcrumbs
