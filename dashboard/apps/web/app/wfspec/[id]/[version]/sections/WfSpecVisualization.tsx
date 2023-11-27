import { WfSpecVisualizer } from '../components/visualizer/WfSpecVisualizer'
import { VersionChanger } from '../components/VersionChanger'

export function WfSpecVisualization({ id, version }:any) {


    return     <section>
        <div className="between">
            <h2>WfSpec visualization</h2>
            <VersionChanger id={id} version={version} />
        </div>


        <WfSpecVisualizer id={id} version={version} />
    </section>
}
