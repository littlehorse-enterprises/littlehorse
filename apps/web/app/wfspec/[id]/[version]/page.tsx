import Link from "next/link"

const WfSpec = ({params}:{params:{id:string, version:number}}) => {
    return <>
     <h1>WfSpec | {params.id} </h1>
     <div className="flex" style={{width:"100%"}}><Link href={'/'}><span className="color-primary">Cluster Overview</span></Link> / <span>{params.id}</span></div>
     <section>
        <h2>WfSpec visualization</h2>
     </section>
    </>
    
}

export default WfSpec