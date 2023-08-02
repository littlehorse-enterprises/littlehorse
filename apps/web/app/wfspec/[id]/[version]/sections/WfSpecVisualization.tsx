import { Button, Label } from "ui"
import { WfRunVisualizer } from "../components/visualizer/WfRunVisualizer"
import { VersionCHnager } from "../components/VersionChanger"

export const WfSpecVisualization = ({id, version}:any) => {


    return     <section>
         <div className="between">
            <h2>WfSpec visualization</h2>
            <VersionCHnager version={version} id={id} />
         </div>
       

       <WfRunVisualizer id={id} version={version} />
    </section>
}