import { Button, Label } from "ui"
import { WfSpecVisualizer } from "../components/visualizer/WfSpecVisualizer"
import { VersionChanger } from "../components/VersionChanger"

export const WfSpecVisualization = ({id, version}:any) => {


    return     <section>
         <div className="between">
            <h2>WfSpec visualization</h2>
            <VersionChanger version={version} id={id} />
         </div>
       

       <WfSpecVisualizer id={id} version={version} />
    </section>
}