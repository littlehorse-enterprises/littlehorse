import Link from "next/link"
import Breadcrumbs from "../../../components/Breadcrumbs"

const WfRun = ({params}:{params:{id:string}}) => {
    return <>

     <h1><span className="lighter">WfRun</span> <span className="line">|</span> {params.id.charAt(0).toUpperCase() + params.id.slice(1)} </h1>

     <Breadcrumbs pwd={[{
                    title: 'Cluster Overview',
                    href: '/'
                }, {
                    title: `WfRun: ${params.id.charAt(0).toUpperCase() + params.id.slice(1)}`,
                    active: true
                }]} />

     <section>
        <h2>WfRun Visualizer</h2>
     </section>
    </>
    
}
export default WfRun