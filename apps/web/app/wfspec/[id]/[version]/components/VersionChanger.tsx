import { Button, Label } from "ui"

interface Props{
    version:string 
    id: string
}
export const VersionChanger = ({version, id}:Props) => {

    return <div title={id} className="btns btns-right">
    <Label>WfSpec VERSION:</Label>
    <Button >Version {version} <img style={{marginLeft:"30px"}} src="/expand_more.svg" /></Button>
</div>
}