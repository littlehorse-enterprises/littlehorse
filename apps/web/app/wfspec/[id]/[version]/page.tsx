import Breadcrumbs from '../../../../components/Breadcrumbs'
import { WorkflowExecutionMetrics } from './sections/WorkflowExecutionMetrics'
import { WfRunSearch } from './sections/WfRunSearch'
import { WfSpecVisualization } from './sections/WfSpecVisualization'

function WfSpec({ params }: { params: { id: string; version: number } }) {
    return (
        <>
            <h1>
                <span className="lighter">WfSpec</span>{' '}
                <span className="line">|</span>{' '}
                {params.id.charAt(0) + params.id.slice(1)}{' '}
            </h1>

            <Breadcrumbs />

            <WfSpecVisualization id={params.id} version={params.version} />
            <WorkflowExecutionMetrics id={params.id} version={params.version} />

            <WfRunSearch id={params.id} version={params.version} />
        </>
    )
}

export default WfSpec
