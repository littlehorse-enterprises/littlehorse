import React from 'react'
import Breadcrumbs from '../../../components/Breadcrumbs'
import { WfRunVisualization } from './sections/WfRunVisualization'

function WfRun({ params }: { params: { id: string } }) {
    return (
        <>
            <h1>
                <span className="lighter">WfRun</span>{' '}
                <span className="line">|</span>{' '}
                {params.id.charAt(0) + params.id.slice(1)}{' '}
            </h1>

            <Breadcrumbs />
            <WfRunVisualization id={params.id} />
        </>
    )
}
export default WfRun
