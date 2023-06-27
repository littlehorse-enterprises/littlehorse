import { WfRunVisualizer } from "../components/visualizer/WfRunVisualizer"

export const WfSpecVisualization = ({id, version}:any) => {


    return     <section>
        
       <h2>WfSpec visualization</h2>

       <WfRunVisualizer id={id} version={version} />
    </section>
}