import { Button, Label } from "ui"
import { WfRunVisualizer } from "../components/visualizer/WfRunVisualizer"

export const WfSpecVisualization = ({id, version}:any) => {


    return     <section>
         <div className="between">
            <h2>WfSpec visualization</h2>
            <div className="btns btns-right">
                <Label>WfSpec VERSION:</Label>
                <Button>Version {version} <img style={{marginLeft:"30px"}} src="/expand_more.svg" /></Button>
            </div>
         </div>
       

       <WfRunVisualizer id={id} version={version} />
    </section>
}