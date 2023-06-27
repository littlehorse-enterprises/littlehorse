import Link from "next/link"
import { WfRunVisualizer } from "./components/visualizer/WfRunVisualizer"
import { WorkflowExecutionMetrics } from "./sections/WorkflowExecutionMetrics"
import { WfRunSearch } from "./sections/WfRunSearch"

const WfSpec = ({params}:{params:{id:string, version:number}}) => {
    return <>
     <h1>WfSpec | {params.id} </h1>

    {/* bread */}
     <div className="flex" style={{width:"100%"}}><Link href={'/'}><span className="color-primary">Cluster Overview</span></Link> / <span>{params.id}</span></div>

     <WfRunVisualizer id={params.id} version={params.version} />

     <WorkflowExecutionMetrics id={params.id} version={params.version} />

     <WfRunSearch />
    </>
    
}

export default WfSpec