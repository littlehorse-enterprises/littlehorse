import Link from "next/link"
import { TaskDefSchemaInformation } from "./sections/TaskDefSchemaInformation"
import { TaskExecutionMetrics } from "./sections/TaskExecutionMetrics"
import { TaskRunSearch } from "./sections/TaskRunSearch"

const WfRun = ({params}:{params:{id:string}}) => {
    return <>
     <h1>TaskDef | {params.id} </h1>

     {/* bread */}
     <div className="flex" style={{width:"100%"}}><Link href={'/'}><span className="color-primary">Cluster Overview</span></Link> / <span>{params.id}</span></div>
     
     <TaskDefSchemaInformation />
     <TaskExecutionMetrics id={params.id} />
     <TaskRunSearch />
     
    </>
}
export default WfRun