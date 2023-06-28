import Link from "next/link"
import { WfRunVisualizer } from "./components/visualizer/WfRunVisualizer"
import { WorkflowExecutionMetrics } from "./sections/WorkflowExecutionMetrics"
import { WfRunSearch } from "./sections/WfRunSearch"
import Breadcrumbs from "../../../../components/Breadcrumbs"

const WfSpec = ({params}:{params:{id:string, version:number}}) => {
    return <>
     <h1><span className="lighter">WfSpec</span> <span className="line">|</span> {params.id.charAt(0).toUpperCase() + params.id.slice(1)} </h1>


    <Breadcrumbs pwd={[{
                    title: 'Cluster Overview',
                    href: '/'
                }, {
                    title: `${params.id.charAt(0).toUpperCase() + params.id.slice(1)}`,
                    active: true
                }]} />

     <WfRunVisualizer id={params.id} version={params.version} />

     <WorkflowExecutionMetrics id={params.id} version={params.version} />

     <WfRunSearch />
    </>
    
}

export default WfSpec