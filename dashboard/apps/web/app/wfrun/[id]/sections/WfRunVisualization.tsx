import { WfRunVisualizer } from '../components/visualizer/WfRunVisualizer'

export function WfRunVisualization({ id }: any) {
    return (
        <section>
            <div className='between'>
                <h2>WfRun visualization</h2>
            </div>

            <WfRunVisualizer wfRunId={id} />
        </section>
    )
}
