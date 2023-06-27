import Link from "next/link"

const WfRun = ({params}:{params:{id:string}}) => {
    return <>
     <h1>WfRun | {params.id} </h1>

     {/* bread */}
     <div className="flex" style={{width:"100%"}}><Link href={'/'}><span className="color-primary">Cluster Overview</span></Link> / <span>{params.id}</span></div>

     <section>
        <h2>WfRun Visualizer</h2>
     </section>
    </>
    
}
export default WfRun