'use client'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { Label } from 'ui'

interface Props{
    version:string 
    id: string
}
export function VersionChanger({ version, id }:Props) { 
  const router = useRouter()

  const [ versions, setVersions ] = useState<string[]>([ version ])
  const getVersions = async () => {
    const res = await fetch('/api/search/wfSpec',{
      method:'POST',
      body: JSON.stringify({
        prefix:id
      }),
    })
    if(res.ok){
      const { results } = await res.json()
      setVersions(results.map( (r:any) => r.version))
    }
        
  }
  const changeV = (version:string) => {
    router.push(`/wfspec/${id}/${version}`)
  }
  useEffect( () => {
    getVersions()
  },[])
  return <div className="btns btns-right" title={id}>
    <Label>WfSpec VERSION:</Label>
    <div className='version_select'>
      <select  onChange={ e => { changeV(e.target.value) }} value={version}>
        {versions.map( v => <option key={v} value={v}>Version {v}</option>)}
      </select>
      <img src="/expand_more.svg" style={{ marginLeft:'30px' }} />
    </div>
  </div>
}