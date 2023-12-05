import Breadcrumbs from '../../../../components/Breadcrumbs'
import { UserTaskDefSchemaInformation } from './sections/userTaskDefSchemaInformation'
import { UserTaskRunSearch } from './sections/userTaskRunSearch'

function WfRun({
    params,
}: {
    params: { id: string; version: string; name: string };
}) {
    return (
        <>
            <h1>
                <span className="lighter">UserTaskDef</span>{' '}
                <span className="line">|</span>{' '}
                {params.id.charAt(0) + params.id.slice(1)}{' '}
            </h1>

            <Breadcrumbs />

            <UserTaskDefSchemaInformation
                id={params.id}
                version={params.version}
            />

            <UserTaskRunSearch id={params.id} version={params.version} />
        </>
    )
}
export default WfRun
