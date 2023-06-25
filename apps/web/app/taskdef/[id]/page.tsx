import Link from "next/link"

const WfRun = ({params}:{params:{id:string}}) => {
    return <>
     <h1>TaskDef | {params.id} </h1>
     <div className="flex" style={{width:"100%"}}><Link href={'/'}><span className="color-primary">Cluster Overview</span></Link> / <span>{params.id}</span></div>
     <section>
        <h2>TaskDef Schema Information</h2>
     </section>
    </>
}
export default WfRun