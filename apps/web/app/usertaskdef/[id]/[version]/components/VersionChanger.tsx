"use client";
import { useRouter } from 'next/navigation';
import { useEffect, useState } from "react"
import { Button, Label } from "ui"

interface Props{
    version:string 
    id: string
}
export const VersionChanger = ({version, id}:Props) => { 
    const router = useRouter();

    const [versions, setVersions] = useState<string[]>([version])
    const getVersions = async () => {
        const res = await fetch('/api/search/userTaskDef',{
            method:'POST',
            body: JSON.stringify({
                prefix:id
            }),
        })
        if(res.ok){
            const {results} = await res.json()
            setVersions(results.map( (r:any) => r.version))
        }
        
    }
    const changeV = (version:string) => {
        router.push(`/usertaskdef/${id}/${version}`);
    }
    useEffect( () => {
        getVersions()
    },[])
    return <div title={id} className="btns btns-right">
    <Label>UserTaskDef VERSION:</Label>
    <div className='version_select'>
        <select  value={version} onChange={ e => changeV(e.target.value)}>
            {versions.map( v => <option key={v} value={v}>Version {v}</option>)}
        </select>
        <img style={{marginLeft:"30px"}} src="/expand_more.svg" />
    </div>
</div>
}